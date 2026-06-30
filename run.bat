@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo     Bus Ticket Reservation System Launcher
echo ===================================================
echo.

:: Define class paths and source paths
set CLASSPATH=lib/*;bin
set SRC_FILES=src\com\reservation\exception\CustomExceptions.java src\com\reservation\model\Bus.java src\com\reservation\model\Booking.java src\com\reservation\model\User.java src\com\reservation\database\DatabaseManager.java src\com\reservation\gui\BusReservationGUI.java src\com\reservation\Main.java

:: Detect Java compiler and runtime
:: Prioritize the Android Studio JBR (which is modern Java 21) to avoid version mismatch
if exist "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" (
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set JAVAC_CMD="C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
        set JAVA_CMD="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
        echo [INFO] Using Java Development Kit from Android Studio (Java 21)
        goto compile
    )
)

:: Fallback: Check if javac and java are in the system PATH
set JAVAC_CMD=javac
set JAVA_CMD=java
where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] Java compiler 'javac' was not found.
    echo Attempting to launch the pre-compiled version directly...
    goto run
)

:compile
:: Create bin folder if missing
if not exist bin (
    mkdir bin
)

echo Compiling Java source files...
%JAVAC_CMD% -cp "lib/*" -d bin %SRC_FILES%
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo Compilation successful.
echo.

:run
:: Verify java runtime command is set/valid
where !JAVA_CMD! >nul 2>nul
if %errorlevel% neq 0 (
    :: Double check if it is an absolute path from Android Studio (which won't be in 'where')
    if not exist !JAVA_CMD! (
        echo [ERROR] Java Runtime could not be found.
        echo Please ensure Java JRE or JDK is installed.
        pause
        exit /b 1
    )
)

echo Launching Bus Ticket Reservation System GUI...
%JAVA_CMD% -cp "bin;lib/*" com.reservation.Main
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application exited with code %errorlevel%.
    pause
)
