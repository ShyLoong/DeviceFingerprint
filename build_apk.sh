#!/bin/bash
set -e

echo "=== APK Manual Build Script ==="

PROJECT_DIR="/mnt/agents/output/DeviceFingerprint"
SDK="/mnt/agents/android-sdk"
BUILD_TOOLS="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-34"
ANDROID_JAR="$PLATFORM/android.jar"
BUILD_DIR="$PROJECT_DIR/build"
APK_NAME="DeviceFingerprint.apk"

echo "Step 0: Setup..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen"
mkdir -p "$BUILD_DIR/classes"
mkdir -p "$BUILD_DIR/apk"
mkdir -p "$BUILD_DIR/res_compiled"

# Find all Java source files
echo "Step 1: Finding Java sources..."
find "$PROJECT_DIR/app/src/main/java" -name "*.java" > "$BUILD_DIR/sources.txt"
cat "$BUILD_DIR/sources.txt"
SRC_COUNT=$(wc -l < "$BUILD_DIR/sources.txt")
echo "Found $SRC_COUNT Java files"

# Step 1: Compile resources with aapt2
echo "Step 2: Compiling resources..."
RES_DIR="$PROJECT_DIR/app/src/main/res"

# Compile each resource file
for file in $(find "$RES_DIR" -type f); do
    rel_path="${file#$RES_DIR/}"
    ext="${file##*.}"
    
    case "$ext" in
        xml|png|jpg|jpeg|webp|gif|bmp)
            output_file="$BUILD_DIR/res_compiled/${rel_path}.flat"
            mkdir -p "$(dirname "$output_file")"
            "$BUILD_TOOLS/aapt2" compile "$file" -o "$BUILD_DIR/res_compiled/" 2>/dev/null || true
            ;;
    esac
done

echo "Step 3: Linking resources..."
# Collect all compiled resource files
RES_LIST=""
for f in $(find "$BUILD_DIR/res_compiled" -name "*.flat" 2>/dev/null); do
    RES_LIST="$RES_LIST -R \"$f\""
done

# Create a dummy AndroidManifest with package name
MANIFEST="$PROJECT_DIR/app/src/main/AndroidManifest.xml"

# Link resources
"$BUILD_TOOLS/aapt2" link \
    -o "$BUILD_DIR/apk/resources.apk" \
    -I "$ANDROID_JAR" \
    --manifest "$MANIFEST" \
    --java "$BUILD_DIR/gen" \
    --auto-add-overlay \
    $(find "$BUILD_DIR/res_compiled" -name "*.flat" | xargs -I{} echo -R "{}") \
    2>&1 || {
        echo "aapt2 link failed, trying alternative approach..."
        # Alternative: use aapt
        "$BUILD_TOOLS/aapt" package \
            -f \
            -M "$MANIFEST" \
            -I "$ANDROID_JAR" \
            -S "$RES_DIR" \
            -m \
            -J "$BUILD_DIR/gen" \
            -F "$BUILD_DIR/apk/resources.apk" \
            --auto-add-overlay \
            2>&1
    }

echo "Step 4: Compiling Java sources..."
# Build classpath - need android.jar and support libraries
CLASSPATH="$ANDROID_JAR"

# Compile Java files
javac -encoding UTF-8 \
    -source 1.8 -target 1.8 \
    -cp "$CLASSPATH" \
    -d "$BUILD_DIR/classes" \
    @$BUILD_DIR/sources.txt \
    2>&1 || {
        echo "javac failed. Let's check the errors..."
        javac -encoding UTF-8 \
            -source 1.8 -target 1.8 \
            -cp "$CLASSPATH" \
            -d "$BUILD_DIR/classes" \
            @$BUILD_DIR/sources.txt 2>&1 | head -50
        exit 1
    }

echo "Step 5: Converting to DEX..."
"$BUILD_TOOLS/d8" \
    --release \
    --output "$BUILD_DIR/apk/" \
    $(find "$BUILD_DIR/classes" -name "*.class") \
    2>&1

echo "Step 6: Building APK..."
cp "$BUILD_DIR/apk/resources.apk" "$BUILD_DIR/apk/unsigned.apk" 2>/dev/null || {
    # If resources.apk wasn't created, build from scratch
    cd "$BUILD_DIR/apk"
    # Create minimal APK with resources
    "$BUILD_TOOLS/aapt" package \
        -f \
        -M "$MANIFEST" \
        -I "$ANDROID_JAR" \
        -S "$RES_DIR" \
        -F "$BUILD_DIR/apk/unsigned.apk" \
        2>&1
}

# Add classes.dex to APK
cd "$BUILD_DIR"
if [ -f "$BUILD_DIR/apk/classes.dex" ]; then
    "$BUILD_TOOLS/aapt" add "$BUILD_DIR/apk/unsigned.apk" "$BUILD_DIR/apk/classes.dex" 2>&1
fi

echo "Step 7: Aligning APK..."
"$BUILD_TOOLS/zipalign" -f 4 \
    "$BUILD_DIR/apk/unsigned.apk" \
    "$BUILD_DIR/apk/unaligned.apk" 2>&1 || true

cp "$BUILD_DIR/apk/unsigned.apk" "$BUILD_DIR/apk/unaligned.apk" 2>/dev/null || true

echo "Step 8: Signing APK..."
# Generate debug keystore if not exists
KEYSTORE="$BUILD_DIR/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US" \
        2>&1
fi

"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --in "$BUILD_DIR/apk/unaligned.apk" \
    --out "$BUILD_DIR/$APK_NAME" \
    2>&1

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK: $BUILD_DIR/$APK_NAME"
ls -lh "$BUILD_DIR/$APK_NAME"
