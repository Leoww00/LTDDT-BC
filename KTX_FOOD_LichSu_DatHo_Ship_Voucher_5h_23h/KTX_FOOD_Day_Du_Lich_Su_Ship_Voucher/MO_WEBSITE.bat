@echo off
chcp 65001 > nul
cd /d "%~dp0"
echo ================================================
echo    KTX FOOD - HE THONG DAT DO AN KY TUC XA
echo ================================================
echo Dang kiem tra JDK va bien dich Java...
where javac >nul 2>nul
if errorlevel 1 (
  echo KHONG TIM THAY JAVAC. Hay cai JDK 17+ va thu lai.
  pause
  exit /b 1
)
if not exist out mkdir out
(for /r src %%f in (*.java) do @echo "%%f") > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  echo Bien dich loi. Gui anh man hinh nay de duoc ho tro.
  pause
  exit /b 1
)
del /q sources.txt >nul 2>nul
echo Dang mo website...
java -cp out main.Main
echo.
echo Website da dung. Nhan phim bat ky de dong cua so.
pause >nul
