*** Settings ***
Library    SeleniumLibrary

*** Variables ***
${URL}        http://localhost:8080/login
${BROWSER}    chrome
${EMAIL}      admin@gmail.com
${PASSWORD}   123456

*** Test Cases ***
Test Login HRM
    Open Browser    ${URL}    ${BROWSER}
    Maximize Browser Window
    Input Text    name=email    ${EMAIL}
    Input Password    name=password    ${PASSWORD}
    Click Button    xpath=//button[@type='submit']
    Sleep    10s