<#
.SYNOPSIS
    blog-main-services 微服务一键启动脚本
.DESCRIPTION
    按顺序启动 MySQL、Nacos、Spring Boot 应用
.PARAMETER SkipMysql
    跳过 MySQL 启动（已在别处运行时使用）
.PARAMETER SkipNacos
    跳过 Nacos 启动
.PARAMETER Port
    应用端口，默认 8080
.EXAMPLE
    .\start.ps1              # 全部启动
    .\start.ps1 -Port 8081   # 指定端口
    .\start.ps1 -SkipMysql   # 跳过 MySQL
#>
param(
    [switch]$SkipMysql,
    [switch]$SkipNacos,
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$MysqlExe = "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysqld.exe"
$MysqlCli = "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysql.exe"
$NacosBin = "$ProjectRoot\nacos\nacos\bin\startup.cmd"

function Write-Step { param($msg) Write-Host "[1/3] $msg" -ForegroundColor Cyan }
function Write-Ok   { param($msg) Write-Host "      $msg" -ForegroundColor Green }
function Write-Warn2 { param($msg) Write-Host "      $msg" -ForegroundColor Yellow }

# ==================== 1. MySQL ====================
if (-not $SkipMysql) {
    Write-Step "启动 MySQL..."
    $mysqlRunning = (netstat -ano | Select-String ":3306.*LISTENING")
    if ($mysqlRunning) {
        Write-Warn2 "MySQL 已在 3306 端口运行，跳过"
    } else {
        Start-Process -FilePath $MysqlExe -ArgumentList "--datadir=`"$ProjectRoot\mysql-data`"", "--port=3306", "--console" -WindowStyle Minimized
        Write-Ok "等待 MySQL 就绪..."
        $retries = 0
        while (-not (netstat -ano | Select-String ":3306.*LISTENING") -and $retries -lt 15) {
            Start-Sleep -Seconds 1
            $retries++
        }
        if ($retries -ge 15) {
            Write-Error "MySQL 启动超时"
            exit 1
        }
        Write-Ok "MySQL 已就绪 (3306)"
    }
} else {
    Write-Warn2 "跳过 MySQL"
}

# ==================== 2. Nacos ====================
if (-not $SkipNacos) {
    Write-Host ""
    Write-Host "[2/3] 启动 Nacos..." -ForegroundColor Cyan
    $nacosRunning = (netstat -ano | Select-String ":8848.*LISTENING")
    if ($nacosRunning) {
        Write-Warn2 "Nacos 已在 8848 端口运行，跳过"
    } else {
        Start-Process -FilePath $NacosBin -ArgumentList "-m", "standalone" -WindowStyle Minimized
        Write-Ok "等待 Nacos 就绪..."
        $retries = 0
        while (-not (netstat -ano | Select-String ":8848.*LISTENING") -and $retries -lt 40) {
            Start-Sleep -Seconds 1
            $retries++
        }
        if ($retries -ge 40) {
            Write-Error "Nacos 启动超时"
            exit 1
        }
        Start-Sleep -Seconds 3
        Write-Ok "Nacos 已就绪 (8848)"
    }
} else {
    Write-Warn2 "跳过 Nacos"
}

# ==================== 3. Spring Boot 应用 ====================
Write-Host ""
Write-Host "[3/3] 启动 blog-services 微服务..." -ForegroundColor Cyan
$env:SERVER_PORT = $Port
Write-Ok "端口: $Port"
Write-Ok "注册到 Nacos: 127.0.0.1:8848"
Write-Ok "正在启动..."

Set-Location $ProjectRoot
.\mvnw.cmd spring-boot:run -pl blog-services
