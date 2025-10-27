package Main;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;


public class Dos {

		
		
		
			
			    
			    private String email;
			    private String password;
			    private String practice;
			    private File excelFile;
			    private File pdfFile;

			    public Dos(String email, String password, String practice, File excelFile, File pdfFile) {
			        this.email = email;
			        this.password = password;
			        this.practice = practice;
			        this.excelFile = excelFile;
			        this.pdfFile = pdfFile;
			    }

			    public void executeAutomation() throws InterruptedException {
			        WebDriver driver = initializeWebDriver();
			        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			        try (FileInputStream fileInputStream = new FileInputStream(excelFile);
			             XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {

			            Sheet sheet = workbook.getSheetAt(0);
			            navigateToPatients(driver, wait);

			            int lastRow = sheet.getLastRowNum();
			            for (int i = 1; i <= lastRow; i++) {
			                Row row = sheet.getRow(i);
			                if (row == null) continue;

			                String patientId = getCellValueAsString(row.getCell(0));
			                if (patientId.isEmpty()) {
			                    System.out.println("🛑 No Patient ID found in row " + (i + 1) + ". Stopping execution.");
			                    break;
			                }

			                System.out.println("🔍 Searching for Patient ID: " + patientId);

			                if (!searchAndProcessPatient(driver, wait, patientId)) {
			                    System.out.println("⚠️ Patient not found, skipping: " + patientId);
			                    writeExecutionStatus(row, "Not Found");
			                    continue;
			                }

			                String rangeStr = processRangeCell(row.getCell(1));
			                String dos = extractDos(row.getCell(2));
			                String fileType = getCellValueAsString(row.getCell(3)); 
			               

			                int retryCount = 100;
			                boolean success = false;
			                while (retryCount-- > 0) {
			                    if (addDocument(driver, wait, dos,fileType, rangeStr)) {
			                        System.out.println("✅ Document added for Patient ID: " + patientId);
			                        writeExecutionStatus(row, "Success");
			                        success = true;
			                        break;
			                    } else {
			                        System.out.println("❌ Failed to add document for " + patientId + ". Retrying...");
			                    }
			                }

			                if (!success) {
			                    System.out.println("🚨 Skipping Patient ID " + patientId + " after multiple failures.");
			                    writeExecutionStatus(row, "Failed");
			                }

			                driver.navigate().refresh();
			                navigateToPatients(driver, wait);
			            }

			        } catch (IOException e) {
			            e.printStackTrace();
			        } finally {
			            driver.quit();
			        }
			    }

			    private WebDriver initializeWebDriver() {
			    	ChromeOptions options = new ChromeOptions();
			    	options.addArguments("--headless"); // Run Chrome without UI
			    	options.addArguments("--window-size=1920,1080"); // Set resolution to avoid element visibility issues

			    	WebDriver driver = new ChromeDriver(options);
			    	options.addArguments("--disable-gpu");
			    	options.addArguments("--disable-software-rasterizer");

			        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

			  
			        driver.get("https://nationwidelog.com/");
			        driver.manage().window().maximize();
			        loginToSite(driver);
			        selectPractice(driver);
			        return driver;
			    }

			    private void loginToSite(WebDriver driver) {
			        driver.findElement(By.name("email")).sendKeys(this.email);
			        driver.findElement(By.name("password")).sendKeys(this.password);
			        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
			    }

			    private void selectPractice(WebDriver driver) {
			        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			        WebElement practiceElement = wait.until(ExpectedConditions.elementToBeClickable(
			                By.xpath("//select[@class='form-select form-select-sm all_prc text-center']")
			        ));
			        new Select(practiceElement).selectByVisibleText(this.practice);
			    }

			    private void navigateToPatients(WebDriver driver, WebDriverWait wait) {
			    	
			    	WebElement element = driver.findElement(By.xpath("//span[text()='Patient(s)']"));
			    	((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
			    	element.click();


			    }

			    private boolean searchAndProcessPatient(WebDriver driver, WebDriverWait wait, String patientId) {
			        try {
			            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
			                    By.xpath("//input[@class='form-control form-control-sm common_selector f_account_number']")
			            ));
			            searchBox.clear();
			            searchBox.sendKeys(patientId);
			            searchBox.sendKeys(Keys.ENTER);

			            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
			            Thread.sleep(2000);

			            for (WebElement table : driver.findElements(By.tagName("table"))) {
			                for (WebElement row : table.findElements(By.tagName("tr"))) {
			                    for (WebElement column : row.findElements(By.tagName("td"))) {
			                        if (column.getText().trim().equals(patientId)) {
			                            System.out.println("✅ Found Patient ID " + patientId);
			                            Thread.sleep(1000);
			                            String xpath = "//a[normalize-space()='" + patientId + "']";
			                            WebElement idElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));

			                            try {
			                                idElement.click();
			                            } catch (Exception e) {
			                                JavascriptExecutor js = (JavascriptExecutor) driver;
			                                js.executeScript("arguments[0].click();", idElement);
			                            }

			                            return true;
			                        }
			                    }
			                }
			            }

			            return false;
			        } catch (Exception e) {
			            e.printStackTrace();
			            return false;
			        }
			    }

			    private boolean addDocument(WebDriver driver, WebDriverWait wait, String dos,String fileType, String rangeStr) {
			        try {
			            WebElement addDocBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@class='btn btn-sm btn-primary adddoc']")));
			            addDocBtn.click();

			            Select fileDropdown = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//select[@class='form-select form-select-sm'])[1]"))));
			            fileDropdown.selectByVisibleText(pdfFile.getName());

			            Select providerDropdown = new Select(driver.findElement(By.xpath("(//select[@class='form-select form-select-sm'])[2]")));
		                providerDropdown.selectByIndex(1);

			            
			            Select fileTypeDropdown = new Select(driver.findElement(By.xpath("(//select[@class='form-select form-select-sm'])[3]")));
		                fileTypeDropdown.selectByVisibleText(fileType);

			            WebElement dosField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@class='form-control form-control-sm date_input_add']")));
			            dosField.clear();
			            dosField.sendKeys(dos + Keys.TAB);
			            
			            WebElement status = driver.findElement(By.xpath("(//select[@class='form-select form-select-sm'])[5]"));
		                new Select(status).selectByVisibleText("Closed");
		                driver.findElement(By.xpath("(//input[@class='form-control form-control-sm'])[1]")).sendKeys(rangeStr);  // Assuming "N/A" as range (can change as per requirements)


			            driver.findElement(By.xpath("//button[@class='btn btn-primary submit_btn']")).click();

			            return true;
			        } catch (Exception e) {
			            e.printStackTrace();
			            return false;
			        }
			    }
			    
			    private String processRangeCell(Cell cell) {
			        String rangeStr = "";
			        if (cell != null) {
			            switch (cell.getCellType()) {
			                case NUMERIC:
			                    double numericValue = cell.getNumericCellValue();
			                    rangeStr = String.valueOf((int) numericValue);  // Convert to integer and store as string
			                    break;
			                case STRING:
			                    rangeStr = cell.getStringCellValue().trim();  // Ensure no leading/trailing spaces
			                    break;
			                case BLANK:
			                    rangeStr = "N/A";
			                    break;
			                default:
			                    rangeStr = "Unknown Type";
			            }
			        } else {
			            rangeStr = "No data";
			        }
			        return rangeStr;
			    }

			    private void writeExecutionStatus(Row row, String status) throws IOException {
			        try (FileInputStream fis = new FileInputStream(excelFile);
			             XSSFWorkbook workbook = new XSSFWorkbook(fis);
			             FileOutputStream fos = new FileOutputStream(excelFile)) {

			            Sheet sheet = workbook.getSheetAt(0);
			            int rowIndex = row.getRowNum();
			            Row updatedRow = sheet.getRow(rowIndex);
			            if (updatedRow == null) {
			                updatedRow = sheet.createRow(rowIndex);
			            }

			            Cell cell = updatedRow.createCell(4, CellType.STRING);
			            cell.setCellValue(status);

			            workbook.write(fos);
			            System.out.println("✅ Status written to Excel: " + status);
			        } catch (IOException e) {
			            e.printStackTrace();
			            System.out.println("❌ Error writing to Excel file: " + e.getMessage());
			        }
			    }

			    private static String getCellValueAsString(Cell cell) {
			        if (cell == null) return "";
			        return switch (cell.getCellType()) {
			            case STRING -> cell.getStringCellValue().trim();
			            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
			            default -> "";
			        };
			    }


			    private String extractDos(Cell cell) {
			        if (cell == null) return "";
			        return (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
			                ? new SimpleDateFormat("MM/dd/yyyy").format(cell.getDateCellValue())
			                : cell.getStringCellValue().trim();
			     }
			}


