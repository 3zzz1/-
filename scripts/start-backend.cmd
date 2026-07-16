@echo off
if not exist "D:\Project\VSworkplace\智慧审计平台系统\logs" mkdir "D:\Project\VSworkplace\智慧审计平台系统\logs"
cd /d "D:\Project\VSworkplace\智慧审计平台系统\backend\ruoyi-admin"
echo start backend at %date% %time% > "D:\Project\VSworkplace\智慧审计平台系统\logs\backend-script.log"
echo cwd=%cd% >> "D:\Project\VSworkplace\智慧审计平台系统\logs\backend-script.log"
call "D:\Program Files\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run >> "D:\Project\VSworkplace\智慧审计平台系统\logs\backend-script.log" 2>&1
echo backend exited at %date% %time% >> "D:\Project\VSworkplace\智慧审计平台系统\logs\backend-script.log"
