# PowerShell script to remove BOM from Java files

$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Checking $($javaFiles.Count) Java files for BOM..."

foreach ($file in $javaFiles) {
    try {
        # Read file content as bytes to check for BOM
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # Check if the file has a BOM (EF BB BF)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            # Remove the BOM by writing back without the first 3 bytes
            $newBytes = $bytes[3..($bytes.Length - 1)]
            [System.IO.File]::WriteAllBytes($file.FullName, $newBytes)
            
            Write-Host "✓ Removed BOM from: $($file.Name)"
        }
    }
    catch {
        Write-Host "✗ Error processing $($file.Name): $_" -ForegroundColor Red
    }
}

Write-Host "`n✅ BOM removal completed!"