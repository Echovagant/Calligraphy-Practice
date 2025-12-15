# PowerShell script to fix encoding of Java files

$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Fixing encoding for $($javaFiles.Count) Java files..."

foreach ($file in $javaFiles) {
    try {
        # Read file content with UTF-8 encoding
        $content = Get-Content -Path $file.FullName -Encoding UTF8 -Raw
        
        # Write back with UTF-8 encoding to ensure proper encoding
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        
        Write-Host "✓ Fixed encoding for: $($file.Name)"
    }
    catch {
        Write-Host "✗ Error processing $($file.Name): $_" -ForegroundColor Red
    }
}

Write-Host "`n✅ Encoding fix completed!"