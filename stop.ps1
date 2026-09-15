<#
.SYNOPSIS
    blog-main-services 微服务一键停止脚本
.DESCRIPTION
    停止 Spring Boot 应用、Nacos、MySQL
.EXAMPLE
    .\stop.ps1   # 停止全部服务
#>

Write-Host "停止 blog-main-services 全部服务..." -ForegroundColor Cyan

# 1. 停止 Spring Boot 应用（通过端口查找）
$appPort = $env:SERVER_PORT
if (-not $appPort) { $appPort = "8080" }
$appPid = (netstat -ano | Select-String ":$appPort.*LISTENING" | Select-Object -First 1)
if ($appPid) {
    $pidStr = ($appPid -split '\s+')[-1]
    Stop-Process -Id $pidStr -Force -ErrorAction SilentlyContinue
    Write-Host "  应用 (端口 $appPort, PID $pidStr) 已停止" -ForegroundColor Green
} else {
    Write-Host "  应用未运行" -ForegroundColor Yellow
}

# 2. 停止 Nacos（通过端口 8848 查找）
$nacosPid = (netstat -ano | Select-String ":8848.*LISTENING" | Select-Object -First 1)
if ($nacosPid) {
    $pidStr = ($nacosPid -split '\s+')[-1]
    Stop-Process -Id $pidStr -Force -ErrorAction SilentlyContinue
    Write-Host "  Nacos (端口 8848, PID $pidStr) 已停止" -ForegroundColor Green
} else {
    Write-Host "  Nacos 未运行" -ForegroundColor Yellow
}

# 3. 停止 MySQL
$mysqlPid = (netstat -ano | Select-String ":3306.*LISTENING" | Select-Object -First 1)
if ($mysqlPid) {
    $pidStr = ($mysqlPid -split '\s+')[-1]
    Stop-Process -Id $pidStr -Force -ErrorAction SilentlyContinue
    Write-Host "  MySQL (端口 3306, PID $pidStr) 已停止" -ForegroundColor Green
} else {
    Write-Host "  MySQL 未运行" -ForegroundColor Yellow
}

Write-Host "全部停止完成" -ForegroundColor Cyan
