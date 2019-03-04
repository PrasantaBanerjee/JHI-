package com.JHI.Script;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.github.bonigarcia.wdm.WebDriverManager;

public class qachange {

	WebDriver driver;
	WebDriverWait wait;
	DesiredCapabilities caps;

	static Properties property=null;
	static Properties prop=null;

	ExtentHtmlReporter reporter;
	ExtentReports extent;
	ExtentTest Dashboard, LandingPages;

	String region, pagename;
	String timestamp = new SimpleDateFormat("EEE MM-dd-yyyy hh-mm-ss a").format(new Date());

	public static void readPropertiesFile() throws IOException {
		property=new Properties();
		InputStream input=new FileInputStream(".\\src\\com\\JHI\\Properties\\config.properties");
		property.load(input);

		prop=new Properties();
		InputStream objrep=new FileInputStream(".\\src\\com\\JHI\\Properties\\ObjectRepository.properties");
		prop.load(objrep);
	}

	public String getPropertyValue(String key) {
		return property.getProperty(key);
	}

	public String getPropValue(String key) {
		return prop.getProperty(key);
	}

	@BeforeClass
	public  void setupReport() throws IOException {
		reporter=new ExtentHtmlReporter("./Reports/Test Report " + timestamp + ".html");
		extent = new ExtentReports();
		extent.attachReporter(reporter);	
		reporter.config().setDocumentTitle("JHInvestments Automation Test Report");
		reporter.config().setReportName("JHInvestments Automation Test Report");
		reporter.config().setTheme(Theme.DARK);
		Dashboard = extent.createTest("Dashboard","Validate Dashboard page.");
	}

	@BeforeMethod
	public void InvokeBrowser() {
		try {	    
			readPropertiesFile();

			/*SELECT BROWSERNAME FROM CONFIG*/
			if(property.getProperty("BrowserName").equalsIgnoreCase("Chrome")) 
			{
				WebDriverManager.chromedriver().setup();
				driver = new ChromeDriver();
				driver.manage().window().maximize();
				caps= DesiredCapabilities.chrome();
			}
			else if(property.getProperty("BrowserName").equalsIgnoreCase("IE")) 
			{
				WebDriverManager.iedriver().setup();
				driver=new InternetExplorerDriver();
				caps= DesiredCapabilities.internetExplorer();
			}
			else if(property.getProperty("BrowserName").equalsIgnoreCase("Firefox")) 
			{
				WebDriverManager.firefoxdriver().setup();
				driver=new FirefoxDriver();	
				caps= DesiredCapabilities.firefox();
			}

			/*SELECT URL FROM CONFIG*/	
			region=property.getProperty("Execute");
			if(region.equalsIgnoreCase("Prod")) {
				driver.get(property.getProperty("ProdURL"));
			}
			else if(region.equalsIgnoreCase("Stage")) {
				driver.get(property.getProperty("StageURL"));
			}
			else if(region.equalsIgnoreCase("QA")) {
				driver.get(property.getProperty("QAURL"));
			}

			Screenshots.captscreenshot(driver,"Screenshot");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(priority=2)
	public void NegativeScenarios() {
		try {		
			FileInputStream fis=new FileInputStream(".\\src\\com\\JHI\\DataTable\\RuntimeScenarios.xlsx");
			@SuppressWarnings("resource")
			XSSFWorkbook wb=new XSSFWorkbook(fis);
			XSSFSheet scenario =wb.getSheet("Mastersheet");
			String loginScenarioExecute=scenario.getRow(1).getCell(2).getStringCellValue();

			if(loginScenarioExecute.equalsIgnoreCase("Yes"))
			{ 
				ExtentTest LoginFail = Dashboard.createNode("Login: Negative Scenario","Login using Invalid credentials.");
				driver.findElement(By.xpath(prop.getProperty("Dashboard"))).click();
				
				XSSFSheet login=wb.getSheet("Login");
				String username=login.getRow(5).getCell(1).getStringCellValue();
				driver.findElement(By.xpath(prop.getProperty("Username"))).sendKeys(username);
				String password=login.getRow(5).getCell(2).getStringCellValue();
				driver.findElement(By.xpath(prop.getProperty("Password"))).sendKeys(password);

				Screenshots.captscreenshot(driver, "Screenshot");
				driver.findElement(By.xpath(prop.getProperty("Loginbutton"))).click();
				Screenshots.captscreenshot(driver, "Screenshot");	
				Thread.sleep(5000);
				boolean acc=driver.findElement(By.xpath(prop.getProperty("AccountsHeader"))).isDisplayed();
				if(acc==true)
				{	
					LoginFail.fail("Logged-in with invalid credentials");
				}
				else
				{
					wait = new WebDriverWait(driver,20);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("LoginErrorMsg"))));

					String MessageContent=driver.findElement(By.xpath(prop.getProperty("LoginErrorMsg"))).getText(); 
					LoginFail.pass("Login failed: " + MessageContent);
				}	
			}
			else {
				System.out.println("Login: Negative scenario skipped");
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(priority=1)
	public void PositiveScenarios() {
		try {
			Dashboard.log(Status.INFO, "OS: " + System.getProperty("os.name"));
			Dashboard.log(Status.INFO, "Browser: " + caps.getBrowserName().toUpperCase());
			Dashboard.log(Status.INFO, "Environment: "+ region);
			Dashboard.log(Status.INFO, "URL: " + driver.getCurrentUrl());
	
		/*	
			String ext=FilenameUtils.getExtension(".\\src\\com\\JHI\\DataTable\\RuntimeScenarios.xlsx");
			System.out.println(ext);
			
			Workbook wbl= WorkbookFactory.create(fis);
			if(wbl instanceof HSSFWorkbook) {
				System.out.println("XLS");
			}
			else if(wbl instanceof SXSSFWorkbook) {
				System.out.println("2");
			} 
			else if(wbl instanceof XSSFWorkbook) {
				System.out.println("XLSX");
			}
		*/
			
			/*LOGIN*/
			FileInputStream fis=new FileInputStream(".\\src\\com\\JHI\\DataTable\\RuntimeScenarios.xlsx");
			@SuppressWarnings("resource")
			XSSFWorkbook wb=new XSSFWorkbook(fis);
			XSSFSheet scenario =wb.getSheet("Mastersheet");
			String loginexecute=scenario.getRow(1).getCell(2).getStringCellValue();

			if(loginexecute.equalsIgnoreCase("Yes"))
			{
				ExtentTest LoginPass = Dashboard.createNode("Login: Positive Scenario","Login using Valid credentials.");
				
				driver.findElement(By.xpath(prop.getProperty("Dashboard"))).click();
				XSSFSheet login=wb.getSheet("Login");

				String username=null, password=null;
				String runenv= property.getProperty("Execute");
				if(runenv.equalsIgnoreCase("Prod")) 
				{
					username=login.getRow(1).getCell(1).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Username"))).sendKeys(username);
					password=login.getRow(1).getCell(2).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Password"))).sendKeys(password);
				}
				else if(runenv.equalsIgnoreCase("Stage")) 
				{
					username=login.getRow(2).getCell(1).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Username"))).sendKeys(username);
					password=login.getRow(2).getCell(2).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Password"))).sendKeys(password);
				} 
				else if(runenv.equalsIgnoreCase("QA")) 
				{
					username=login.getRow(3).getCell(1).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Username"))).sendKeys(username);
					password=login.getRow(3).getCell(2).getStringCellValue();
					driver.findElement(By.xpath(prop.getProperty("Password"))).sendKeys(password);
				} 

				Screenshots.captscreenshot(driver, "Screenshot");
				driver.findElement(By.xpath(prop.getProperty("Loginbutton"))).click();
				Screenshots.captscreenshot(driver, "Screenshot");	
				Thread.sleep(5000);
				boolean acc=driver.findElement(By.xpath(prop.getProperty("AccountsHeader"))).isDisplayed();
				if(acc==true)
				{	
					LoginPass.pass("Login was successful with valid credentials");
					Screenshots.captscreenshot(driver, "Screenshot");


					/*
				ExtentTest AddFund = Dashboard.createNode("Add Fund","Validate Add Fund functionality from Dashboard page.");
				WebElement AddFundIcon = driver.findElement(By.xpath(prop.getProperty("AddFundIcon")));
				@SuppressWarnings("unused")
				Boolean AddIcon=AddFundIcon.isDisplayed();			
				if (AddIcon=true) 
				 {
						AddFund.log(Status.INFO, "Add Fund Icon Displayed");
						AddFundIcon.click();
						AddFund.log(Status.INFO, "Clicked on - Add Fund Icon");

						WebDriverWait waitForSearchBox = new WebDriverWait(driver,20);
						waitForSearchBox.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("SearchFund"))));

						WebElement SearchBox = driver.findElement(By.xpath(prop.getProperty("SearchFund")));	

						XSSFSheet search =wb.getSheet("Search");
						String fundone=search.getRow(2).getCell(0).getStringCellValue();
						SearchBox.sendKeys(fundone);
						AddFund.log(Status.INFO, "Searching for - " + fundone);

						WebDriverWait waitForResult = new WebDriverWait(driver,20);
						waitForResult.until(ExpectedConditions.textToBe(By.xpath("//span[contains(text(),'JCUCX')]"), "JCUCX"));
						Thread.sleep(3000);

						WebElement checkboxClick=driver.findElement(By.xpath(prop.getProperty("Checkbox")));
						checkboxClick.click();

						WebElement AddButton=driver.findElement(By.xpath(prop.getProperty("AddButton")));
						@SuppressWarnings("unused")
						boolean addButtonDisplay=AddButton.isEnabled();
						if(addButtonDisplay=true) {
							AddButton.click();
							AddFund.log(Status.INFO, "Clicked on ADD Button");
							Thread.sleep(8000);
						}
						else {
							AddFund.warning("ADD Button not enabled");
							}	
				}
				else {
					AddFund.warning("Add Fund Icon not Displayed");
					 }		
					 */	
				}
				else {
					String MessageContent=driver.findElement(By.xpath(prop.getProperty("LoginErrorMsg"))).getText(); 
					LoginPass.fail("Login failed: " + MessageContent);
				}	
			}
			else {
				System.out.println("Login: Positive Scenario skipped");
			}

			/*INVESTMENTS*/
			String investmentsexec=scenario.getRow(2).getCell(2).getStringCellValue();
			if(investmentsexec.equalsIgnoreCase("Yes"))
			{
				ExtentTest Investment=extent.createTest("Investments","Validate Investments & Fund Details page.");
				ExtentTest InvestmentPage = Investment.createNode("Investments page","Validate Investments page.");
				ExtentTest FundDetailsPage = null;

				WebElement InvHeader=driver.findElement(By.xpath(prop.getProperty("InvHeader")));
				@SuppressWarnings("unused")
				Boolean Invpresent= InvHeader.isDisplayed();
				if(Invpresent=true) 
				{
					InvHeader.click();
					Screenshots.captscreenshot(driver, "Screenshot");	
				}
				else {	
					Investment.log(Status.INFO, "Investments is not present in header");
					Screenshots.captscreenshot(driver, "Screenshot");	
				}

				wait = new WebDriverWait(driver,20);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("InvTicker"))));

				XSSFSheet search =wb.getSheet("Search");
				String searchticker=search.getRow(1).getCell(0).getStringCellValue();

				driver.findElement(By.xpath(prop.getProperty("SearchBox"))).sendKeys(searchticker);
				InvestmentPage.log(Status.INFO, "Searching for " + searchticker + " fund");
				Screenshots.captscreenshot(driver, "Screenshot");	

				WebElement invticker= driver.findElement(By.xpath(prop.getProperty("InvTicker")));
				@SuppressWarnings("unused")
				Boolean tickerpresent=invticker.isDisplayed();
				if(tickerpresent=false) 
				{
					InvestmentPage.fail(searchticker + " fund was not found");
				}
				else 
				{
					InvestmentPage.pass(searchticker + " fund was found");
					invticker.click();
					InvestmentPage.log(Status.INFO, "Clicked on " + searchticker + " fund");
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("ObjectiveSection"))));
					Screenshots.captscreenshot(driver, "Screenshot");	

					/*FUND DETAILS*/
					FundDetailsPage = Investment.createNode("Fund Details page","Validate Fund Details page.");
					WebElement overview = driver.findElement(By.xpath(prop.getProperty("Overview")));
					@SuppressWarnings("unused")
					Boolean overviewpresent = overview.isDisplayed();
					if(overviewpresent=true) 
					{
						overview.click();
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Overview tab present & clicked");
						Thread.sleep(3000);
					}
					else {
						FundDetailsPage.fail("Overview tab not found");
						Screenshots.captscreenshot(driver, "Screenshot");	
					}

					/*Related section*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,1250)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement related=driver.findElement(By.xpath(prop.getProperty("Related")));
					@SuppressWarnings("unused")
					Boolean relatedpresent=related.isDisplayed();
					if(relatedpresent=true) 
					{
						FundDetailsPage.pass("Related section present");
						Thread.sleep(2000);		
					}
					else {
						FundDetailsPage.fail("Related section not present");
						Screenshots.captscreenshot(driver, "Screenshot");	
					}

					WebElement relatedviewpoint=driver.findElement(By.xpath(prop.getProperty("RelatedViewpoints")));
					@SuppressWarnings("unused")
					Boolean viewpointpresent=relatedviewpoint.isDisplayed();
					if(viewpointpresent=true)
					{
						FundDetailsPage.pass("Viewpoints present");
					}
					else {
						FundDetailsPage.fail("Viewpoints not present");
					}

					WebElement keyfacts = driver.findElement(By.xpath(prop.getProperty("KeyFacts")));
					@SuppressWarnings("unused")
					Boolean keyfactpresent = keyfacts.isDisplayed();
					if(keyfactpresent=true) 
					{
						keyfacts.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Key facts present & clicked");
					}
					else {
						Screenshots.captscreenshot(driver, "Screenshot");
						FundDetailsPage.fail("Key facts not present");
					}

					WebElement performance = driver.findElement(By.xpath(prop.getProperty("Performance")));
					@SuppressWarnings("unused")
					Boolean perfpresent = performance.isDisplayed();
					if(perfpresent=true) 
					{
						performance.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Performance present & clicked");
					}
					else {
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.fail("Performance not present");
					}

					/*Calendar Year Returns*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,980)");
					Screenshots.captscreenshot(driver, "Screenshot");	
					Thread.sleep(2000);	

					/*Hypothetical Growth*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,780)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					/*Market cycle performance*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,780)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					/*Risk & Return*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,780)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement ratings = driver.findElement(By.xpath(prop.getProperty("Ratings")));
					@SuppressWarnings("unused")
					Boolean ratingspresent = ratings.isDisplayed();
					if(ratingspresent=true) 
					{
						ratings.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Ratings present & clicked");
					}
					else {
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.fail("Ratings not present");
					}

					/*Morning star Percentile Rankings*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,850)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement characteristics = driver.findElement(By.xpath(prop.getProperty("Characteristics")));
					@SuppressWarnings("unused")
					Boolean charpresent = characteristics.isDisplayed();
					if(charpresent=true) 
					{
						characteristics.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Characteristics tab present & clicked");
					}
					else {
						Screenshots.captscreenshot(driver, "Screenshot");
						FundDetailsPage.fail("Characteristics tab not present");
					}

					/*Risk & Performance Measures*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,680)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement distributions = driver.findElement(By.xpath(prop.getProperty("Distributions")));
					@SuppressWarnings("unused")
					Boolean distpresent = distributions.isDisplayed();
					if(distpresent=true) 
					{
						distributions.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Distributions tab present & clicked");
					}
					else {
						System.out.println("Distributions not found");
						Screenshots.captscreenshot(driver, "Screenshot");
						FundDetailsPage.fail("Distributions tab not present");
					}

					/*Price History*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,430)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement feesexpenses = driver.findElement(By.xpath(prop.getProperty("FeesExpenses")));
					@SuppressWarnings("unused")
					Boolean feespresent = feesexpenses.isDisplayed();
					if(feespresent=true)
					{
						feesexpenses.click();
						Thread.sleep(3000);
						Screenshots.captscreenshot(driver, "Screenshot");	
						FundDetailsPage.pass("Fees & Expenses present & clicked");
					}
					else {
						Screenshots.captscreenshot(driver, "Screenshot");
						FundDetailsPage.fail("Fees & Expenses not present");
					}

					/*Risk Disclosures*/
					((JavascriptExecutor) driver).executeScript("window.scrollBy(0,300)");
					Screenshots.captscreenshot(driver, "Screenshot");
					Thread.sleep(2000);	

					WebElement riskdisc= driver.findElement(By.xpath(prop.getProperty("RiskDisclosure")));
					@SuppressWarnings("unused")
					Boolean riskdiscpresent = riskdisc.isDisplayed();
					if(riskdiscpresent=true) 
					{
						FundDetailsPage.pass("Risk Disclosures title present");
					}
					else {
						FundDetailsPage.fail("Risk Disclosures title not present");
					}

					WebElement riskdisccontent= driver.findElement(By.xpath(prop.getProperty("RiskDiscContent")));
					String disccontent=riskdisccontent.getText();
					if(disccontent.isEmpty()) 
					{
						FundDetailsPage.fail("Risk Disclosure Content not present");
					}
					else {
						FundDetailsPage.pass("Risk Disclosure Content present");
					}
				}
			}
			else {
				System.out.println("SKIPPED Investments");
			}

			/*VIEWPOINTS*/
			String viewpointExec=scenario.getRow(3).getCell(2).getStringCellValue();
			if(viewpointExec.equalsIgnoreCase("Yes"))
			{
				ExtentTest Viewpoints=extent.createTest("Viewpoints","Validate Viewpoints page.");

				WebElement ViewpointsHeader=driver.findElement(By.xpath(prop.getProperty("ViewpointHeader")));
				@SuppressWarnings("unused")
				Boolean viewpointPresent= ViewpointsHeader.isDisplayed();
				if(viewpointPresent=true) 
				{
					ViewpointsHeader.click();
					Screenshots.captscreenshot(driver, "Screenshot");	

					WebElement MarkIntTab= driver.findElement(By.xpath(prop.getProperty("MITab")));
					MarkIntTab.click();
					Viewpoints.log(Status.INFO, "Clicked on Market Intelligence tab");
					wait = new WebDriverWait(driver,20);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("MIPageTitle"))));	
					Screenshots.captscreenshot(driver, "Screenshot");	
					pagename= driver.findElement(By.xpath(prop.getProperty("MIPageTitle"))).getText();

					LandingPages=extent.createTest("Landing Pages","Validate Landing Pages.");
					LandingPages.log(Status.INFO, region+" VIEWPOINTS Page: Clicked on Market Intelligence tab");
					LandingPage();
					driver.navigate().back();
					Screenshots.captscreenshot(driver, "Screenshot");	

					WebElement WMRecapTab=driver.findElement(By.xpath(prop.getProperty("WMRTab")));
					WMRecapTab.click();
					Viewpoints.log(Status.INFO, "Clicked on Weekly Market Recap tab");
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("WMRPageTitle"))));	
					Screenshots.captscreenshot(driver, "Screenshot");	
					pagename= driver.findElement(By.xpath(prop.getProperty("WMRPageTitle"))).getText();

					LandingPages.log(Status.INFO, region+" VIEWPOINTS Page: Clicked on Weekly Market Recap tab");
					LandingPage();
					driver.navigate().back();
					Screenshots.captscreenshot(driver, "Screenshot");	

					WebElement GlobalOutlookTab=driver.findElement(By.xpath(prop.getProperty("GMOTab")));
					GlobalOutlookTab.click();
					Viewpoints.log(Status.INFO, "Clicked on Global Market Outlook tab");
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("GMOPageTitle"))));	
					Screenshots.captscreenshot(driver, "Screenshot");	
					pagename= driver.findElement(By.xpath(prop.getProperty("GMOPageTitle"))).getText();

					LandingPages.log(Status.INFO, region+" VIEWPOINTS Page: Clicked on Global Market Outlook tab");
					LandingPage();
					driver.navigate().back();
					Screenshots.captscreenshot(driver, "Screenshot");	

					WebElement ViewpointOnView=driver.findElement(By.xpath(prop.getProperty("ViewpointOnView")));
					String titleOnView = ViewpointOnView.getText();
					Viewpoints.log(Status.INFO, "Viewpoint: "+ titleOnView);
					ViewpointOnView.click();
					Viewpoints.log(Status.INFO, "Clicked on the Viewpoint");
					Thread.sleep(5000);

					WebElement ViewpointOnClick=driver.findElement(By.xpath(prop.getProperty("ViewpointOnClick")));
					String titleOnClick = ViewpointOnClick.getText();

					if(titleOnView.equalsIgnoreCase(titleOnClick)) {
						Viewpoints.pass("Successfully navigated to: " + titleOnClick);	
						Screenshots.captscreenshot(driver, "Screenshot");	
					}
					else {
						Viewpoints.fail("Navigated to: " + titleOnClick);	
						Screenshots.captscreenshot(driver, "Screenshot");	
					}

					WebElement Logo=driver.findElement(By.xpath(prop.getProperty("Logo")));
					@SuppressWarnings("unused")
					boolean logoPresent=Logo.isDisplayed();
					if(logoPresent=true) {
						Viewpoints.pass("Asset Management Logo displayed");	
						Screenshots.captscreenshot(driver, "Screenshot");	
					}
					else {
						Viewpoints.fail("Asset Management Logo not displayed");
						Screenshots.captscreenshot(driver, "Screenshot");	
					}	

					try {
						List <WebElement> elements  = driver.findElements(By.xpath(prop.getProperty("SocialShare")));
						Viewpoints.log(Status.INFO,"Number of elements: " +elements.size());
						for (int i=0; i<elements.size();i++)
						{
							String winHandleBefore = driver.getWindowHandle();
							elements.get(i).click();
							Viewpoints.log(Status.INFO,"Clicked on: " + elements.get(i).getText() + " link");
							/*	String SocialURL=null;*/
							for(String winHandle : driver.getWindowHandles())
							{
								driver.switchTo().window(winHandle);
								/*	SocialURL=driver.getCurrentUrl();*/
							}
							/*	Viewpoints.pass("Navigated to: " + SocialURL);*/
							driver.close();
							driver.switchTo().window(winHandleBefore);
						}
					} catch (NoSuchSessionException e) {
						e.printStackTrace();
					}

					driver.navigate().back();

					driver.findElement(By.xpath(prop.getProperty("SubscribeNow"))).click();
					WebDriverWait wait=new WebDriverWait(driver,30);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("FirstNameForm"))));
					driver.findElement(By.xpath(prop.getProperty("FirstNameForm"))).sendKeys("FirstName123");
					driver.findElement(By.xpath(prop.getProperty("LastNameForm"))).sendKeys("LastName456");	
					driver.findElement(By.xpath(prop.getProperty("DropdownForm"))).click();
					List <WebElement> elements  = driver.findElements(By.xpath(prop.getProperty("DropdownOptions")));
					for (int i=0; i<elements.size();i++)
					{
						System.out.println(elements.get(i).getText());
					}
					elements.get(2).click();
					driver.findElement(By.xpath(prop.getProperty("EMailForm"))).sendKeys("qa@test.com");	
					driver.findElement(By.xpath(prop.getProperty("FirmForm"))).sendKeys("QA");
					driver.findElement(By.xpath(prop.getProperty("SubmitForm"))).click();
					WebElement errorOnClick=driver.findElement(By.xpath(prop.getProperty("ErrorOnSubmitForm")));
					System.out.println("Error Message displayed as: " + errorOnClick.getText());


					/*verify cancel & cross buttons*/

				}
				else {	
					Viewpoints.log(Status.INFO, "Viewpoints is not present in header");
					Screenshots.captscreenshot(driver, "Screenshot");	
				}
			}
			else {
				System.out.println("SKIPPED Viewpoints");
			}

			/*RESOURCES*/
			String resourcesExec=scenario.getRow(4).getCell(2).getStringCellValue();
			if(resourcesExec.equalsIgnoreCase("Yes"))
			{
				ExtentTest Resources=extent.createTest("Resources","Validate Resources page.");

				WebElement resourceHeader=driver.findElement(By.xpath(prop.getProperty("ResourcesHeader")));
				resourceHeader.click();
				Screenshots.captscreenshot(driver, "Screenshot");

				WebElement FinanceAdviseButton=driver.findElement(By.xpath(prop.getProperty("FinancialButton")));
				FinanceAdviseButton.click();
				Resources.log(Status.INFO, "Clicked on Financial Advisors Link");
				Screenshots.captscreenshot(driver, "Screenshot");
				Thread.sleep(3000);

				WebElement ETFLink=driver.findElement(By.xpath(prop.getProperty("ETFLink")));
				ETFLink.click();

				ExtentTest ETFPage=Resources.createNode("ETF","Validate ETF page.");
				Screenshots.captscreenshot(driver, "Screenshot");

				LandingPages.log(Status.INFO, region + " RESOURCES Page: Clicked on ETF tab");
				wait = new WebDriverWait(driver,30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(prop.getProperty("ETFPageTitle"))));	

				pagename=driver.findElement(By.xpath(prop.getProperty("ETFPageTitle"))).getText();
				if(pagename.contains("ETF")) {
					ETFPage.pass("ETF Page successfully opened");
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					ETFPage.fail("ETF Page failed to open");
					Screenshots.captscreenshot(driver, "Screenshot");
				}

				WebElement ETFVideo=driver.findElement(By.xpath(prop.getProperty("ETFVideoLink")));
				if(ETFVideo.isDisplayed()) {
					ETFPage.pass("Video displayed to the Left");
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					ETFPage.fail("Video not displayed to the Left");
					Screenshots.captscreenshot(driver, "Screenshot");
				}

				WebElement ETFButton=driver.findElement(By.xpath(prop.getProperty("SeeETFButton")));
				if(ETFButton.isDisplayed()) {
					ETFPage.pass("See our ETFs Button displayed");
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					ETFPage.fail("See our ETFs Button not displayed");
					Screenshots.captscreenshot(driver, "Screenshot");
				}

				LandingPage();
				driver.navigate().back();

				ExtentTest ESGPage=Resources.createNode("ESG","Validate ESG page.");
				WebElement ESGLink=driver.findElement(By.xpath(prop.getProperty("ESGLink")));
				ESGLink.click();

				LandingPages.log(Status.INFO, region+" RESOURCES Page: Clicked on ESG tab");

				Screenshots.captscreenshot(driver, "Screenshot");
				ESGPage.pass("ESG Page successfully opened.");
				pagename=driver.findElement(By.xpath(prop.getProperty("ESGPageTitle"))).getText();
				LandingPage();
				driver.navigate().back();
			}

			else {
				System.out.println("SKIPPED Resources");
			}

			/*ABOUT US*/
			String aboutusExec=scenario.getRow(5).getCell(2).getStringCellValue();
			if(aboutusExec.equalsIgnoreCase("Yes"))
			{
				ExtentTest AboutUs=extent.createTest("About Us","Validate About Us page.");
				ExtentTest SocialMedia;
				SocialMedia=AboutUs.createNode("Social Media","Validate Social Media section.");

				WebElement AboutUsHeader=driver.findElement(By.xpath(prop.getProperty("AboutUsHeader")));
				AboutUsHeader.click();
				Screenshots.captscreenshot(driver, "Screenshot");

				/*TWITTER*/
				WebElement Twitter=driver.findElement(By.xpath(prop.getProperty("TwitterLink")));
				WebElement linkName1 = driver.findElement(By.xpath(prop.getProperty("IconTitle1")));
				Twitter.click();
				SocialMedia.log(Status.INFO, "Clicked on: " + linkName1.getText());
				Thread.sleep(3000);
				String onClickTwitter=driver.getCurrentUrl();
				if(onClickTwitter.contains("twitter")) 
				{
					SocialMedia.pass("Successfully navigated to Twitter page ("+ onClickTwitter + ")");	
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					SocialMedia.fail("Failed to navigate to Twitter page. Navigated to: "+ onClickTwitter);
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				Thread.sleep(3000);
				driver.navigate().back();
				Thread.sleep(3000);
				Screenshots.captscreenshot(driver, "Screenshot");

				/*FACEBOOK*/
				WebElement Facebook=driver.findElement(By.xpath(prop.getProperty("FacebookLink")));
				WebElement linkName2 = driver.findElement(By.xpath(prop.getProperty("IconTitle2")));
				Facebook.click();
				SocialMedia.log(Status.INFO, "Clicked on: " + linkName2.getText());
				Thread.sleep(3000);
				String onClickFacebook=driver.getCurrentUrl();
				if(onClickFacebook.contains("facebook")) 
				{
					SocialMedia.pass("Successfully navigated to Facebook page ("+ onClickFacebook + ")");	
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					SocialMedia.fail("Failed to navigate to Facebook page. Navigated to: "+ onClickFacebook);
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				Thread.sleep(3000);
				driver.navigate().back();
				Thread.sleep(3000);
				Screenshots.captscreenshot(driver, "Screenshot");

				/*LINKEDIN*/
				WebElement Linkedin=driver.findElement(By.xpath(prop.getProperty("LinkedinLink")));
				WebElement linkName3 = driver.findElement(By.xpath(prop.getProperty("IconTitle3")));
				Linkedin.click();
				SocialMedia.log(Status.INFO, "Clicked on: " + linkName3.getText());
				Thread.sleep(3000);
				String onClickLinkedin=driver.getCurrentUrl();
				if(onClickLinkedin.contains("linkedin")) 
				{
					SocialMedia.pass("Successfully navigated to Linked-in page ("+ onClickLinkedin + ")");	
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					SocialMedia.fail("Failed to navigate to Linked-in page. Navigated to: "+ onClickLinkedin);
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				Thread.sleep(3000);
				driver.navigate().back();
				Thread.sleep(3000);
				Screenshots.captscreenshot(driver, "Screenshot");

				/*YOUTUBE*/
				WebElement Youtube=driver.findElement(By.xpath(prop.getProperty("YoutubeLink")));
				WebElement linkName4 = driver.findElement(By.xpath(prop.getProperty("IconTitle4")));
				Youtube.click();
				SocialMedia.log(Status.INFO, "Clicked on: " + linkName4.getText());
				Thread.sleep(3000);
				String onClickYoutube=driver.getCurrentUrl();
				if(onClickYoutube.contains("youtube")) 
				{
					SocialMedia.pass("Successfully navigated to Youtube page ("+ onClickYoutube + ")");	
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				else {
					SocialMedia.fail("Failed to navigate to Youtube page. Navigated to: "+ onClickYoutube);
					Screenshots.captscreenshot(driver, "Screenshot");
				}
				Thread.sleep(3000);
				driver.navigate().back();
				Thread.sleep(3000);
				Screenshots.captscreenshot(driver, "Screenshot");
			}
			else {
				System.out.println("SKIPPED About Us");
			}
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void LandingPage() throws InterruptedException, IOException {
		String PageURL=driver.getCurrentUrl();

		/*PROD*/
		if(region.equalsIgnoreCase("Prod") & PageURL.contains("www") || region.equalsIgnoreCase("Prod") & PageURL.contains("wmr")) {
			LandingPages.pass("Successfully navigated to " + pagename + " (" + PageURL + ")");
		}
		else if(region.equalsIgnoreCase("Prod") & PageURL.contains("stg") || region.equalsIgnoreCase("Prod") & PageURL.contains("qa") ) {
			LandingPages.warning("Navigated to "+ PageURL + " from " + region);
		}

		/*STAGE*/					
		if(region.equalsIgnoreCase("Stage") & PageURL.contains("stg")) {
			LandingPages.pass(" Successfully navigated to " + pagename + " (" + PageURL + ")");
		}
		else if(region.equalsIgnoreCase("Stage") & PageURL.contains("www") || region.equalsIgnoreCase("Stage") & PageURL.contains("qa") || region.equalsIgnoreCase("Stage") & PageURL.contains("wmr") ) {
			LandingPages.warning("Navigated to "+ PageURL + " from " + region);
		}

		/*QA*/					
		if(region.equalsIgnoreCase("QA") & PageURL.contains("qa")) {
			LandingPages.pass("Successfully navigated to " +  pagename + " (" + PageURL + ")");
		}
		else if(region.equalsIgnoreCase("QA") & PageURL.contains("stg") || region.equalsIgnoreCase("QA") & PageURL.contains("www") || region.equalsIgnoreCase("QA") & PageURL.contains("wmr") ) {
			LandingPages.warning("Navigated to "+ PageURL + " from " + region);
		}

		List <WebElement> elements  = driver.findElements(By.xpath(prop.getProperty("JumpLinkHeader")));
		LandingPages.log(Status.INFO,"Number of tabs: " + elements.size());
		for (int i=0; i<elements.size();i++)
		{
			if(elements.get(i).isEnabled()) 
			{
				elements.get(i).click();
				LandingPages.log(Status.INFO,"Clicked on: " + elements.get(i).getText());
				Screenshots.captscreenshot(driver, "Screenshot");
				Thread.sleep(3000);
			}
			else {
				LandingPages.log(Status.INFO,elements.get(i).getText() + " is not enabled");
				Screenshots.captscreenshot(driver, "Screenshot");	   
			}
		}
	}

	@AfterMethod
	public void CloseBrowser() {
		if (driver != null) {
			driver.quit();
		}
	}

	@AfterClass
	public void endReport() throws IOException {
		extent.flush();
		/*Files.createDirectories(Paths.get("./Reports/Test Report " + timestamp));*/
		Desktop.getDesktop().open(new File(".\\Reports"));
	}
}

/*run script from bat file*/
/*take img to .pdf
headless browser
1st tab: google
2nd tab: yahoo
	TESTNG LISTENER
	CHROME FIREFOX HTML UNIT DRIVER OPTIONS
	JS EXEC
	OPEN LINK IN NEW TAB
	FLUENT WAIT
	cucumber
/*data from csv exe xls xlsx dbms odbc*/