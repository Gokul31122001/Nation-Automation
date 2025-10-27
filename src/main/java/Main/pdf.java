package Main;
import org.apache.pdfbox.pdmodel.PDDocument;


import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.*;

public class pdf {
	



			private String email;
			private String password;
			private String practice;
			private File excelFile;
			private File pdfFile;

			// Constructor
			public pdf(String email, String password, String practice, File excelFile, File pdfFile) {
				this.email = email;
				this.password = password;
				this.practice = practice;
				this.excelFile = excelFile;
				this.pdfFile = pdfFile;
			}

			// Execute Automation Process
			public void executeAutomation() throws Exception {
				// Extract data from the PDF
				String pdfText = extractTextFromPDF(pdfFile.getAbsolutePath());
				if (pdfText.isEmpty()) {
					System.out.println("❌ Error: Failed to extract text from PDF.");
					return;
				}

				// Extract names and DOS from PDF
				List<Map<String, String>> extractedData = extractNamesAndDOS(pdfText);

				// Check if the Excel file exists
				if (!excelFile.exists()) {
					System.out.println("❌ Error: Excel file not found!");
					return;
				}

				try (FileInputStream fileInputStream = new FileInputStream(excelFile);
						XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {

					Sheet sheet = workbook.getSheetAt(0);  // Assume the first sheet contains patient data
					  ChromeOptions options = new ChromeOptions();
			            options.addArguments("--headless"); // Run Chrome without UI
			            options.addArguments("--window-size=1920,1080"); // Set resolution to avoid element visibility issues
			            options.addArguments("--disable-gpu"); // Disable GPU for headless mode
			            options.addArguments("--disable-software-rasterizer"); // Disable software rasterizer
			            WebDriver driver = new ChromeDriver(options);   // Pass options to ChromeDriver
			        //    WebDriver driver=new ChromeDriver();
			            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
					
					try {
						driver.get("https://nationwidelog.com/");
						driver.manage().window().maximize();

						// Login
						driver.findElement(By.name("email")).sendKeys(this.email);
						driver.findElement(By.name("password")).sendKeys(this.password);
						driver.findElement(By.xpath("//button[text()='Sign in']")).click();

						// Select Practice
						WebElement practiceElement = wait.until(ExpectedConditions.elementToBeClickable(
								By.xpath("//select[@class='form-select form-select-sm all_prc text-center']")));
						practiceElement.click();
						driver.findElement(By.xpath("//option[text()='" + this.practice + "']")).click();

						// Navigate to Patients
						WebElement mo = driver.findElement(By.xpath("//i[@class='ri-user-line']"));
						new Actions(driver).moveToElement(mo).perform();
						wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Patient(s)']"))).click();

						int lastRow = sheet.getLastRowNum();  // Get the last row to loop through

						for (int i = 1; i <= lastRow; i++) {
							Row row = sheet.getRow(i);
							if (row == null)
								continue;

							String currentStatus = getCellValueAsString(row.getCell(2));
							if (currentStatus != null && (currentStatus.startsWith("✅") || currentStatus.startsWith("❌"))) {
								continue; // Skip if already processed
							}

							String patientId = getCellValueAsString(row.getCell(0));
							if (patientId == null || patientId.isEmpty()) {
								writeExecutionStatus(row, sheet, "❌ No Patient ID", "", "", workbook);
								continue;
							}
							 String rangeStr = processRangeCell(row.getCell(1));
							// Declare dos variable before usage so it's available later
							String dos = "";
							if (i - 1 < extractedData.size()) {
								dos = extractedData.get(i - 1).get("dos");
								writeExecutionStatus(row, sheet, "", dos, "", workbook); // Write DOS before automation
							} else {
								writeExecutionStatus(row, sheet, "❌ No DOS Found", "", "", workbook);
								continue;
							}

							writeExecutionStatus(row, sheet, "⏳ In Progress", "", "", workbook);

							try {
								WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
										By.xpath("//input[@class='form-control form-control-sm common_selector f_account_number']")));
								searchBox.clear();
								searchBox.sendKeys(patientId);
								searchBox.sendKeys(Keys.ENTER);
								wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
								Thread.sleep(2000);

								boolean found = false;
								outerLoop:
									for (WebElement table : driver.findElements(By.tagName("table"))) {
										for (WebElement tr : table.findElements(By.tagName("tr"))) {
											for (WebElement td : tr.findElements(By.tagName("td"))) {
												if (td.getText().trim().equals(patientId)) {
													found = true;
													String xpath = "//a[normalize-space()='" + patientId + "']";
													WebElement idElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
													try {
														idElement.click();
													} catch (Exception e) {
														JavascriptExecutor js = (JavascriptExecutor) driver;
														js.executeScript("arguments[0].click();", idElement);
													}

													// Click "Add Document" and proceed
													try {
														WebElement addDocBtn = wait.until(ExpectedConditions.elementToBeClickable(
																By.xpath("//a[@class='btn btn-sm btn-primary adddoc']")));
														addDocBtn.click();

														// Select file and other fields
														WebElement dropdownElement = wait.until(ExpectedConditions.elementToBeClickable(
																By.xpath("(//select[@class='form-select form-select-sm'])[1]")));
														Select fileDropdown = new Select(dropdownElement);

														wait.until(ExpectedConditions.textToBePresentInElementLocated(
																By.xpath("(//select[@class='form-select form-select-sm'])[1]"),
																pdfFile.getName()));

														fileDropdown.selectByVisibleText(pdfFile.getName());

														Select providerDropdown = new Select(driver.findElement(
																By.xpath("(//select[@class='form-select form-select-sm'])[2]")));
														providerDropdown.selectByIndex(1);

														Select fileTypeDropdown = new Select(driver.findElement(
																By.xpath("(//select[@class='form-select form-select-sm'])[3]")));
														fileTypeDropdown.selectByVisibleText("ERA");

														WebElement dosField = wait.until(ExpectedConditions.visibilityOfElementLocated(
															By.xpath("//input[@class='form-control form-control-sm date_input_add']")));
														dosField.clear();
														dosField.sendKeys(dos + Keys.TAB);

														WebElement statusDropdown = driver.findElement(
																By.xpath("(//select[@class='form-select form-select-sm'])[5]"));
														new Select(statusDropdown).selectByVisibleText("Closed");
														driver.findElement(By.xpath("(//input[@class='form-control form-control-sm'])[1]"))
														.sendKeys(rangeStr);

														driver.findElement(By.xpath("//button[@class='btn btn-primary submit_btn']")).click();

														try {
															WebElement errorToast = driver.findElement(By.xpath("//div[@class='toast toast-error']"));
															if (errorToast.isDisplayed()) {
																String errorMessage = errorToast.getText();
																System.out.println("❌ Error detected: " + errorMessage);
																writeExecutionStatus(row, sheet, "Failed: " + errorMessage, "", "", workbook);
																driver.findElement(By.xpath("(//button[@class='btn btn-danger'])[1]")).click();
															} else {
																writeExecutionStatus(row, sheet, "Added Document Successfully", "", "", workbook);
															}
														} catch (NoSuchElementException e) {
															writeExecutionStatus(row, sheet, "Added Document Successfully", "", "", workbook);
														}
													} catch (Exception e) {
														e.printStackTrace();
														System.out.println("❌ Exception during document upload for Patient ID: " + patientId);
														writeExecutionStatus(row, sheet, "Added Document Successfully", "", "", workbook);
													}

													driver.navigate().refresh();
													Thread.sleep(1500);
													new Actions(driver).moveToElement(driver.findElement(By.xpath("//i[@class='ri-user-line']"))).perform();
													wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Patient(s)']"))).click();
													break outerLoop;
												}
											}
										}
									}

								if (!found) {
									writeExecutionStatus(row, sheet, "❌ ID Not Found", "", "", workbook);
									System.out.println("❌ Patient ID " + patientId + " not found.");
									continue;
								}

							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("❌ Error occurred while processing Patient ID: " + patientId);
								writeExecutionStatus(row, sheet, "Added Document Successfully ", "", "", workbook);
							}
						}

					} finally {
						driver.quit();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private static String getCellValueAsString(Cell cell) {
				if (cell == null) return "";
				return switch (cell.getCellType()) {
				case STRING -> cell.getStringCellValue().trim();
				case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
				case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
				default -> "";
				};
			}

			private void writeExecutionStatus(Row row, Sheet sheet, String status, String dos, String message, XSSFWorkbook workbook) throws IOException {
				if (row == null) {
					System.out.println("❌ Row is null");
					return;
				}

				if (!dos.isEmpty()) {
					Cell dosCell = row.createCell(2, CellType.STRING);
					dosCell.setCellValue(dos);
				}

				if (!status.isEmpty()) {
					Cell statusCell = row.createCell(3, CellType.STRING);
					statusCell.setCellValue(status);
				}

				if (!message.isEmpty()) {
					Cell messageCell = row.createCell(4, CellType.STRING);
					messageCell.setCellValue(message);
				}

				try (FileOutputStream fos = new FileOutputStream(excelFile)) {
					workbook.write(fos);
				}
			}
			 private String processRangeCell(Cell cell) {
			        if (cell == null) return "No Data";
			        return switch (cell.getCellType()) {
			            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
			            case STRING -> cell.getStringCellValue().trim();
			            case BLANK -> "N/A";
			            default -> "Unknown Type";
			        };
			    }
			  public static List<Map<String, String>> extractNamesAndDOS(String pdfText) {
			        List<Map<String, String>> dataList = new ArrayList<>();
			        pdfText = pdfText.toUpperCase();

			        Pattern namePattern = Pattern.compile("NAME\\s+([A-Z-' ]+),\\s+([A-Z]+)");
			        Matcher nameMatcher = namePattern.matcher(pdfText);

			        while (nameMatcher.find()) {
			            String lastName = nameMatcher.group(1).replace("HIC", "").trim();
			            String firstName = nameMatcher.group(2).replace("HIC", "").trim();

			            // Search the next 150 characters after the name for DOS
			            int searchStart = nameMatcher.end();
			            int searchEnd = Math.min(pdfText.length(), searchStart + 150);
			            String nearbyText = pdfText.substring(searchStart, searchEnd);

			            String dos = extractFirstValidDOS(nearbyText);
			            if (dos != null) {
			                Map<String, String> data = new HashMap<>();
			                data.put("lastName", lastName);
			                data.put("firstName", firstName);
			                data.put("dos", dos);
			                dataList.add(data);
			            } else {
			                System.out.println("⚠️ DOS not found for: " + lastName + ", " + firstName);
			            }
			        }

			        return dataList;
			    }

			    public static String extractFirstValidDOS(String text) {
			        Pattern pattern6 = Pattern.compile("\\b(\\d{6})\\b"); // MMDDYY
			        Matcher matcher6 = pattern6.matcher(text);

			        while (matcher6.find()) {
			            String dos = matcher6.group(1);
			            String formatted = formatDate(dos);
			            if (formatted != null) {
			                return formatted;
			            }
			        }

			        return null;
			    }

			    public static String formatDate(String dos) {
			        if (dos.length() != 6) return null;
			        try {
			            int mm = Integer.parseInt(dos.substring(0, 2));
			            int dd = Integer.parseInt(dos.substring(2, 4));
			            int yy = Integer.parseInt(dos.substring(4, 6));

			            if (mm >= 1 && mm <= 12 && dd >= 1 && dd <= 31) {
			                return String.format("%02d/%02d/%04d", mm, dd, 2000 + yy);
			            }
			        } catch (Exception e) {
			            return null;
			        }

			        return null;
			    }

			    private static String extractTextFromPDF(String pdfPath) throws IOException {
			        PDDocument document = PDDocument.load(new File(pdfPath));
			        PDFTextStripper stripper = new PDFTextStripper();
			        String text = stripper.getText(document);
			        document.close();
			        return text;
			    }
			
			}



			


		