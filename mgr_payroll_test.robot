*** Settings ***
Library    SeleniumLibrary

*** Variables ***
${BASE_URL}          http://localhost:8080
${LOGIN_URL}         ${BASE_URL}/login
${PAYSLIP_URL}       ${BASE_URL}/manager/payroll/payslips
${BROWSER}           chrome
${USERNAME}          mgr1
${PASSWORD}          123456
${TIMEOUT}           10s

*** Test Cases ***
Manager Open Payroll Payslip List
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}

Manager Search Payslip By Employee Name
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Input Text    xpath=//input[@placeholder='Tên nhân viên...']    Nguyễn
    Click Element    xpath=//button[contains(.,'Search')]
    Sleep    2s

Manager Filter Payslip By Status
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Select From List By Label    xpath=(//select)[3]    Tất cả
    Click Element    xpath=//button[contains(.,'Search')]
    Sleep    2s

Manager Open Add Employee Modal
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=//a[contains(.,'+ Add Employee')] | //button[contains(.,'+ Add Employee')]
    Sleep    2s

Manager Open First Payslip Update
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=(//table//a[contains(@href,'/edit') or .//i[contains(@class,'fa-pen') or contains(@class,'fa-pencil')]])[1] | (//table//button[.//i[contains(@class,'fa-pen') or contains(@class,'fa-pencil')]])[1]
    Sleep    2s

Manager Open First Payslip Detail
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=(//table//a[contains(@href,'/manager/payroll/payslips/') or .//i[contains(@class,'fa-eye')]])[1] | (//table//button[.//i[contains(@class,'fa-eye')]])[1]
    Sleep    2s

Manager Click First Payslip Delete Or Reject
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=(//table//a[contains(@href,'delete') or .//i[contains(@class,'fa-trash')]])[1] | (//table//button[.//i[contains(@class,'fa-trash')]])[1]
    Sleep    2s

Manager Bulk Approve Selected Payslip
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=(//table//input[@type='checkbox'])[2]
    Click Button    Approve
    Sleep    2s

Manager Bulk Reject Selected Payslip
    Open Login Page
    Login As Manager
    Go To    ${PAYSLIP_URL}
    Wait Until Page Contains    Payroll Payslips    ${TIMEOUT}
    Click Element    xpath=(//table//input[@type='checkbox'])[2]
    Click Button    Reject
    Sleep    2s

*** Keywords ***
Open Login Page
    Open Browser    ${LOGIN_URL}    ${BROWSER}
    Maximize Browser Window
    Set Selenium Timeout    ${TIMEOUT}
    Wait Until Page Contains Element    name=username    ${TIMEOUT}

Login As Manager
    Input Text        name=username    ${USERNAME}
    Input Password    name=password    ${PASSWORD}
    Click Button      xpath=//button[@type='submit']
    Sleep    2s