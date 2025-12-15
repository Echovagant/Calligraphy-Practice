# FixImportsBasic.ps1 - Basic import fixes for Java files

# Set source directory
$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Fixing basic imports for $($javaFiles.Count) Java files..."

foreach ($file in $javaFiles) {
    Write-Host "Processing file: $($file.Name)"
    
    try {
        # Read file content
        $content = Get-Content -Path $file.FullName -Raw
        
        # 1. Fix PostConstruct import - critical fix
        $content = $content.Replace('import javax.annotation.PostConstruct;', 'import jakarta.annotation.PostConstruct;')
        
        # 2. Remove unused Nullable imports - critical fix
        $content = $content.Replace('import javax.annotation.Nullable;', '')
        $content = $content.Replace('import jakarta.annotation.Nullable;', '')
        $content = $content.Replace('import lombok.Nullable;', '')
        
        # 3. Add Spring Nullable import if needed
        if ($content.Contains('@Nullable') -and -not $content.Contains('import org.springframework.lang.Nullable;')) {
            $content = "import org.springframework.lang.Nullable;`n$content"
        }
        
        # Write back to file
        Set-Content -Path $file.FullName -Value $content
    } catch {
        Write-Error "Error processing file $($file.Name): $_"
    }
}

Write-Host "Basic import fixes completed!"