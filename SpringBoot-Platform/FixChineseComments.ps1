# PowerShell script to fix Chinese comments in Java files

$sourceDir = "src/main/java"

# Get all Java files
$javaFiles = Get-ChildItem -Path $sourceDir -Filter "*.java" -Recurse

Write-Host "Fixing Chinese comments in $($javaFiles.Count) Java files..."

foreach ($file in $javaFiles) {
    try {
        # Read file content with UTF-8 encoding
        $content = Get-Content -Path $file.FullName -Encoding UTF8 -Raw
        
        # Fix common Chinese encoding issues in validation messages
        $content = $content -replace "鐢ㄦ埛鍚嶄笉鑳戒负绌?", "用户名不能为空"
        $content = $content -replace "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-50涓瓧绗︿箣闂?", "用户名长度必须在3-50个字符之间"
        $content = $content -replace "瀵嗙爜涓嶈兘涓虹┖", "密码不能为空"
        $content = $content -replace "瀵嗙爜闀垮害蹇呴』鍦?-255涓瓧绗︿箣闂?", "密码长度必须在6-255个字符之间"
        
        # Fix any other common encoding issues
        $content = $content -replace "鎴?", ""
        $content = $content -replace "瑕?", ""
        $content = $content -replace "闈炴?", ""
        $content = $content -replace "瑕?", ""
        $content = $content -replace "闇€", ""
        $content = $content -replace "鑳?", ""
        $content = $content -replace "鏄?", ""
        $content = $content -replace "涓?", ""
        $content = $content -replace "鐨?", ""
        $content = $content -replace "涓嶈兘", "不能为空"
        $content = $content -replace "蹇呴』", "必须"
        $content = $content -replace "闀垮害", "长度"
        $content = $content -replace "涓瓧绗?", "个字符"
        $content = $content -replace "涔嬮棿", "之间"
        
        # Write back with UTF-8 encoding
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        
        Write-Host "✓ Fixed Chinese comments in: $($file.Name)"
    }
    catch {
        Write-Host "✗ Error processing $($file.Name): $_" -ForegroundColor Red
    }
}

Write-Host "`n✅ Chinese comments fix completed!"