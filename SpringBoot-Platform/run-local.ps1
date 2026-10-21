<#
    本地启动脚本：把 config/local-secrets.properties 里的密钥读成环境变量，
    再执行 Maven。密钥只存在于当前进程的环境变量里，**从不回显**。

    用法：
      .\run-local.ps1                 # 启动后端（mvn -o spring-boot:run）
      .\run-local.ps1 -Task test      # 跑测试（mvn -o test）
      .\run-local.ps1 -Task compile   # 只编译
#>
param(
    [ValidateSet('run', 'test', 'compile')]
    [string]$Task = 'run',
    [switch]$Online
)

$ErrorActionPreference = 'Stop'
$moduleDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$secretsFile = Join-Path $moduleDir 'config\local-secrets.properties'

if (-not (Test-Path $secretsFile)) {
    Write-Host "找不到本地密钥文件: $secretsFile" -ForegroundColor Red
    Write-Host "请先复制模板并填值：" -ForegroundColor Yellow
    Write-Host "  Copy-Item 'config\local-secrets.properties.example' 'config\local-secrets.properties'"
    exit 1
}

# 读入 KEY=VALUE，逐条注入环境变量。值不回显。
$injected = 0
foreach ($line in [System.IO.File]::ReadAllLines($secretsFile)) {
    $trimmed = $line.Trim()
    if ($trimmed -eq '' -or $trimmed.StartsWith('#')) { continue }
    $idx = $trimmed.IndexOf('=')
    if ($idx -lt 1) { continue }
    $key = $trimmed.Substring(0, $idx).Trim()
    $value = $trimmed.Substring($idx + 1).Trim()
    if ($key -eq '' -or $value -eq '') {
        Write-Host "跳过空值: $key" -ForegroundColor Yellow
        continue
    }
    Set-Item -Path "env:$key" -Value $value
    $injected++
}
Write-Host "已注入 $injected 个环境变量（值不回显）" -ForegroundColor DarkGray

$goal = switch ($Task) {
    'run' { 'spring-boot:run' }
    'test' { 'test' }
    'compile' { 'compile' }
}
$mvnArgs = @()
if (-not $Online) { $mvnArgs += '-o' }
$mvnArgs += @('-B', $goal)

Write-Host "执行: mvn $($mvnArgs -join ' ')" -ForegroundColor DarkGray
Push-Location $moduleDir
try {
    & mvn @mvnArgs
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
