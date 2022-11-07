@REM Copyright (c) Microsoft. All rights reserved.
@REM Licensed under the MIT license. See LICENSE file in the project root for full license information.

set build-root=%~dp0..

SET isPullRequestBuild=true
if "%TARGET_BRANCH%" == "$(System.PullRequest.TargetBranch)" (SET isPullRequestBuild=false)

cd %build-root%\iot-e2e-tests\android
call gradle wrapper --gradle-version 6.8.3
call gradlew :clean :app:clean :app:assembleDebug

if %ERRORLEVEL% NEQ 0 (
  echo "Gradle build failed with error code %ERRORLEVEL%"
  exit 1
)

call gradlew :app:assembleDebugAndroidTest -PIOTHUB_CONNECTION_STRING=%IOTHUB_CONNECTION_STRING% -PIOTHUB_CONN_STRING_INVALIDCERT=%IOTHUB_CONN_STRING_INVALIDCERT% -PIS_BASIC_TIER_HUB=%IS_BASIC_TIER_HUB% -PIS_PULL_REQUEST=%isPullRequestBuild% -PIOTHUB_CLIENT_ID=%IOTHUB_CLIENT_ID% -PIOTHUB_CLIENT_SECRET=%IOTHUB_CLIENT_SECRET% -PMSFT_TENANT_ID=%MSFT_TENANT_ID% -PRECYCLE_TEST_IDENTITIES=true

if %ERRORLEVEL% NEQ 0 (
  echo "Gradle build failed with error code %ERRORLEVEL%"
  exit 1
)
