package com.JHI.Script;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Screenshots {
	
	public static String captscreenshot(WebDriver driver, String screenshotname) throws IOException {
		TakesScreenshot ts = (TakesScreenshot) driver;
		String timestamp = new SimpleDateFormat("EEE MM-dd-yyyy hh-mm-ss a").format(new Date());
		File source=ts.getScreenshotAs(OutputType.FILE);
		String destination="./Screenshots/"+ screenshotname + " " + timestamp +".png";
		File target=new File(destination);
		FileUtils.copyFile(source, target );
		return destination;
	}
}
