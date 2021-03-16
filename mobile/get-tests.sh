#!/bin/bash
#adb shell rm /sdcard/allure-results/*
#adb shell rm /sdcard/allure-results
#rm -rf app/build/allure-results/*
#./gradlew connectedAndroidTest
pushd app/build
adb pull /sdcard/allure-results
cd allure-results
popd
for A in `ls -1 *.json`
do
  #workaround for kotlin-allure bug
  sed -i 's/"testCaseId".*//ig' $A
  sed -i 's/"rerunOf".*//ig' $A
done
popd
./gradlew allureReport
rm -rf allure-report
mv app/build/reports/allure-report allure-report
git add allure-report
