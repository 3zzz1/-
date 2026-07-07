@echo off
REM ============================================
REM 智慧审计平台 - 数据库初始化脚本
REM 自动依次执行所有 SQL 文件
REM ============================================

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=ry-vue
set DB_USER=root
set DB_PASS=1234

echo ========================================
echo  智慧审计平台 - 数据库初始化
echo  数据库: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo ========================================
echo.

REM 1. 创建数据库
echo [1/5] 创建数据库 %DB_NAME%...
mysql -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS \`%DB_NAME%\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 无法连接 MySQL，请检查 MySQL 是否运行、用户名密码是否正确
    pause
    exit /b 1
)
echo [1/5] 数据库创建成功！

REM 2. RuoYi 基础表
echo [2/5] 执行 RuoYi 基础表...
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%~dp0ry_20260417.sql" 2>nul
echo [2/5] 完成！

REM 3. 整改业务表
echo [3/5] 执行审计整改业务表...
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%~dp002_rectification.sql" 2>nul
echo [3/5] 完成！

REM 4. 权限扩展表
echo [4/5] 执行权限扩展表...
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%~dp003_permission_extend.sql" 2>nul
echo [4/5] 完成！

REM 5. 菜单数据
echo [5/5] 执行菜单初始化数据...
mysql -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%~dp004_rectification_menu.sql" 2>nul
echo [5/5] 完成！

echo.
echo ========================================
echo  数据库初始化全部完成！
echo  数据库: %DB_NAME%
echo  表数量: RuoYi基础表 + 9张整改表 + 3张权限扩展表
echo ========================================
pause
