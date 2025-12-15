# FixImportsSimple.ps1 - Fix imports in Java files

# Set source directory
$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Fixing imports for $($javaFiles.Count) Java files..."

foreach ($file in $javaFiles) {
    Write-Host "Processing file: $($file.FullName)"
    
    try {
        # Read file content
        $content = Get-Content -Path $file.FullName -Raw
        
        # 1. Fix PostConstruct import
        $content = $content -replace 'import javax.annotation.PostConstruct;', 'import jakarta.annotation.PostConstruct;'
        $content = $content -replace 'javax.annotation.PostConstruct', 'jakarta.annotation.PostConstruct'
        
        # 2. Remove unused Nullable imports
        $content = $content -replace 'import javax.annotation.Nullable;', ''
        $content = $content -replace 'import jakarta.annotation.Nullable;', ''
        $content = $content -replace 'import lombok.Nullable;', ''
        
        # 3. Use Spring's Nullable consistently
        if ($content -match '@Nullable') {
            if (-not ($content -match 'import org.springframework.lang.Nullable;')) {
                $content = "import org.springframework.lang.Nullable;`n$content"
            }
        }
        
        # 4. Remove duplicate imports
        $lines = $content -split '\r?\n'
        $uniqueLines = @()
        foreach ($line in $lines) {
            if (-not ($uniqueLines -contains $line)) {
                $uniqueLines += $line
            }
        }
        $content = $uniqueLines -join "`n"
        
        # 5. Fix empty lines
        $content = $content -replace '(
?
){3,}', "`n`n"
        
        # Write back to file
        Set-Content -Path $file.FullName -Value $content -NoNewline
    } catch {
        Write-Error "Error processing file $($file.FullName): $_"
    }
}

Write-Host "Fixing imports completed!"