# PowerShell script to fix imports and annotations in Java files

$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Found $($javaFiles.Count) Java files to process..."

foreach ($file in $javaFiles) {
    try {
        # Read file content
        $content = Get-Content -Path $file.FullName -Raw
        
        # Remove all @NonNull annotations (various variants)
        $content = $content -replace "@NonNull", ""
        $content = $content -replace "@Nonnull", ""
        $content = $content -replace "@NotNull", ""
        
        # Remove various @NonNull import statements
        $content = $content -replace "import org\.springframework\.lang\.NonNull;", ""
        $content = $content -replace "import javax\.annotation\.Nonnull;", ""
        $content = $content -replace "import jakarta\.annotation\.Nonnull;", ""
        $content = $content -replace "import lombok\.NonNull;", ""
        
        # Unify @Nullable imports to use Spring's version
        $content = $content -replace "import javax\.annotation\.Nullable;", "import org.springframework.lang.Nullable;"
        $content = $content -replace "import jakarta\.annotation\.Nullable;", "import org.springframework.lang.Nullable;"
        
        # Fix PostConstruct and PreDestroy imports
        $content = $content -replace "import javax\.annotation\.PostConstruct;", "import jakarta.annotation.PostConstruct;"
        $content = $content -replace "import javax\.annotation\.PreDestroy;", "import jakarta.annotation.PreDestroy;"
        
        # Fix any direct uses of javax.annotation (shouldn't be needed, but just in case)
        $content = $content -replace "javax\.annotation\.PostConstruct", "jakarta.annotation.PostConstruct"
        $content = $content -replace "javax\.annotation\.PreDestroy", "jakarta.annotation.PreDestroy"
        
        # Fix多余的空格
        $content = $content -replace "\s+@Override", "`n    @Override"
        $content = $content -replace "`n\s*`n\s*`n", "`n`n"
        
        # Write the cleaned content back to the file
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        
        Write-Host "✓ Processed: $($file.Name)"
    }
    catch {
        Write-Host "✗ Error processing $($file.Name): $_" -ForegroundColor Red
    }
}

Write-Host "`n✅ Annotation cleanup completed!"