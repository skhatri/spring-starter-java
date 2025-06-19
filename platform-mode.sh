#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_PROPERTIES="$SCRIPT_DIR/gradle.properties"
SETTINGS_FILE="$SCRIPT_DIR/settings.gradle.kts"
POKEMON_BUILD_FILE="$SCRIPT_DIR/pokemon-service/build.gradle.kts"

function usage() {
    echo "Usage: $0 [local|published] [version]"
    echo ""
    echo "Modes:"
    echo "  local     - Use ../starter-platform as composite build (default)"
    echo "  published - Use published platform from repository"
    echo ""
    echo "Examples:"
    echo "  $0 local                    # Use local platform"
    echo "  $0 published               # Use published platform with current version"
    echo "  $0 published 0.2.0         # Use published platform version 0.2.0"
    echo ""
    echo "Current configuration:"
    grep "platform\." "$GRADLE_PROPERTIES" 2>/dev/null || echo "  No platform configuration found"
}

function set_local_mode() {
    echo "🔧 Switching to LOCAL platform mode..."
    
    # Update gradle.properties
    sed -i.bak "s/^platform\.source=.*/platform.source=local/" "$GRADLE_PROPERTIES"
    
    # Copy local settings file
    if [ -f "$SCRIPT_DIR/settings-local.gradle.kts" ]; then
        cp "$SCRIPT_DIR/settings-local.gradle.kts" "$SETTINGS_FILE"
        echo "   ✓ Copied settings-local.gradle.kts to settings.gradle.kts"
    fi
    
    # Update pokemon-service build file for local mode
    sed -i.bak 's/implementation("com.github.starter:starter-core:.*")/implementation("com.github.starter:starter-core")/' "$POKEMON_BUILD_FILE"
    
    echo "✅ Platform mode set to LOCAL"
    echo "   - Using ../starter-platform as composite build"
    echo "   - Convention plugins loaded from local build-logic"
    echo "   - Dependencies resolved from local platform project"
    echo ""
    echo "💡 Benefits:"
    echo "   ✓ Instant feedback on platform changes"
    echo "   ✓ No need to publish for development"
    echo "   ✓ Automatic dependency resolution"
    echo "   ✓ Gradle daemon can optimize across projects"
}

function set_published_mode() {
    local version="${1:-$(grep "platform.version=" "$GRADLE_PROPERTIES" 2>/dev/null | cut -d'=' -f2)}"
    version="${version:-0.1.0}"
    
    echo "🚀 Switching to PUBLISHED platform mode..."
    
    # Update gradle.properties
    sed -i.bak "s/^platform\.source=.*/platform.source=published/" "$GRADLE_PROPERTIES"
    sed -i.bak "s/^platform\.version=.*/platform.version=$version/" "$GRADLE_PROPERTIES"
    
    # Copy published settings file
    if [ -f "$SCRIPT_DIR/settings-published.gradle.kts" ]; then
        cp "$SCRIPT_DIR/settings-published.gradle.kts" "$SETTINGS_FILE"
        echo "   ✓ Copied settings-published.gradle.kts to settings.gradle.kts"
    fi
    
    # Update pokemon-service build file for published mode
    sed -i.bak "s/implementation(\"com.github.starter:starter-core.*\")/implementation(\"com.github.starter:starter-core:$version\")/" "$POKEMON_BUILD_FILE"
    
    echo "✅ Platform mode set to PUBLISHED"
    echo "   - Using platform version: $version"
    echo "   - Convention plugins from published artifacts"
    echo "   - Dependencies from published artifacts"
    echo ""
    echo "💡 Benefits:"
    echo "   ✓ Locked platform version for stability"
    echo "   ✓ Faster build times (no platform compilation)"
    echo "   ✓ CI/CD friendly configuration"
    echo "   ✓ No need to have platform source code"
    echo ""
    echo "⚠️  Remember to publish platform first:"
    echo "   cd ../starter-platform && ./gradlew publishToMavenLocal"
}

function publish_platform() {
    echo "📦 Publishing platform to local repository..."
    
    if [ ! -d "../starter-platform" ]; then
        echo "❌ Platform directory not found: ../starter-platform"
        exit 1
    fi
    
    (cd ../starter-platform && ./gradlew publishToMavenLocal)
    
    echo "✅ Platform published successfully"
}

function verify_build() {
    echo "🔍 Verifying build configuration..."
    
    echo "Current platform settings:"
    grep "platform\." "$GRADLE_PROPERTIES" 2>/dev/null || echo "  No platform configuration found"
    echo ""
    
    echo "Testing build..."
    ./gradlew pokemon-service:compileJava --console=plain
    
    echo "✅ Build verification successful"
}

function show_status() {
    echo "📊 Current Platform Configuration:"
    echo ""
    
    local platform_source=$(grep "platform.source=" "$GRADLE_PROPERTIES" 2>/dev/null | cut -d'=' -f2 || echo "unknown")
    local platform_version=$(grep "platform.version=" "$GRADLE_PROPERTIES" 2>/dev/null | cut -d'=' -f2 || echo "unknown")
    
    echo "Mode: $platform_source"
    echo "Version: $platform_version"
    echo ""
    
    if [ "$platform_source" = "local" ]; then
        echo "✓ Using composite build from ../starter-platform"
        echo "✓ Convention plugins from build-logic"
        echo "✓ Live dependency resolution"
    elif [ "$platform_source" = "published" ]; then
        echo "✓ Using published artifacts"
        echo "✓ Fixed version: $platform_version"
        echo "✓ No platform source required"
    fi
    
    echo ""
    echo "Settings file: $(basename "$(readlink "$SETTINGS_FILE" 2>/dev/null || echo "$SETTINGS_FILE")")"
}

case "${1:-}" in
    "local")
        set_local_mode
        verify_build
        ;;
    "published")
        set_published_mode "$2"
        echo ""
        echo "🔄 You may need to publish the platform first:"
        echo "   $0 publish"
        ;;
    "publish")
        publish_platform
        ;;
    "verify")
        verify_build
        ;;
    "status")
        show_status
        ;;
    "help"|"-h"|"--help")
        usage
        ;;
    "")
        show_status
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo ""
        usage
        exit 1
        ;;
esac 