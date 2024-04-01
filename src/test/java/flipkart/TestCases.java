package flipkart;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestCases {
    // Declare ChromeDriver instance as a class member for access across methods
    private ChromeDriver driver;

    // HashMap to store review count along with the review text for later retrieval
    // (used in testCase03)
    private HashMap<Integer, String> reviewsMap = new HashMap<>();

    // Method to initialize WebDriver before running tests
    @BeforeTest
    public void driverInitialization() {
        // Setting up ChromeDriver
        WebDriverManager.chromedriver().timeout(30).setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    // Method to close WebDriver after running tests
    @AfterTest
    public void endTest() {
        System.out.println("End Test: TestCases");
        driver.close();
        driver.quit();
    }

    // Test case to search for Washing Machine, apply filter, and print number of
    // products with rating greater than or equal to 4
    @Test(description = "Go to www.flipkart.com. Search “Washing Machine”. Sort by popularity and print the count of items with rating less than or equal to 4 stars.")
    @Parameters({"textToSearch1", "filterName1"})
    public void testCase01(String textToSearch, String filterName) throws InterruptedException {
        System.out.println("Start Test case: Search Washing Machine");
        // Navigate to flipkart landing page
        driver.get("https://www.flipkart.com/");

        // Find the search box and enter the search term "Washing Machine"
        WebElement searchBox = driver.findElement(By.name("q"));
        enterInTextBox(driver, searchBox, textToSearch);
        searchBox.sendKeys(Keys.ENTER);

        // Apply "Popularity" filter
        applyFilter(filterName);

        // Wait for 3 seconds for filter to be applied
        Thread.sleep(3000);

        // Verify filter application and print the number of products with rating
        // greater than or equal to 4
        System.out.println("Filter Applied Successfully : " + verifyFilterApplySuccess("Popularity"));
        printNumberOfProductRatingGraterThanEqual(4);
    }

    // Test case to search for iPhone and print details of discounted products
    @Test(description = "Search “iPhone”, print the Titles and discount % of items with more than 17% discount")
    @Parameters({"textToSearch2", "discountPercent"})
    public void testCase02(String textToSearch, int discount) {
        System.out.println("Start Test case: Search IPhone");

        // Navigate to flipkart landing page
        driver.get("https://www.flipkart.com/");

        // Find the search box and enter the search term "IPhone"
        WebElement searchBox = driver.findElement(By.name("q"));
        enterInTextBox(driver, searchBox, textToSearch);
        searchBox.sendKeys(Keys.ENTER);

        // Find all elements with class "_3Ay6Sb" which contains discount information
        List<WebElement> totalElements = driver.findElements(By.xpath("//div[@class=\"_3Ay6Sb\"]/span"));

        // Print title of discounted products with discount greater than or equal to 17%
        printTitleOfdiscountedProduct(totalElements, discount);
    }

    // Test case to search for Coffee Mug and print details of top 5 products based
    // on reviews
    @Test(description = "Search “Coffee Mug”, select 4 stars and above, and print the Title and image URL of the 5 items with highest number of reviews")
    @Parameters({"textToSearch3"})
    public void testCase03(String textToSearch) throws InterruptedException {
        System.out.println("Start Test case: Search Coffee Mug");
        driver.get("https://www.flipkart.com/");

        // Find the search box and enter the search term "Coffee Mug"
        WebElement searchBox = driver.findElement(By.name("q"));
        enterInTextBox(driver, searchBox, textToSearch);
        searchBox.sendKeys(Keys.ENTER);
        Thread.sleep(2000);
        // apply filer by clicking checkBox of "4★ & above" in customer ratings section
        String xpathExpression = "(//div[@class='_4921Z t0pPfW'])[1]//div[@class='_24_Dny']";
        driver.findElement(By.xpath(xpathExpression)).click();
        Thread.sleep(2000);

        // Define xpath to identify elements containing product rating followed by
        // review count
        By productRatingBy = By.xpath("//span[starts-with(@id, 'productRating_')]//following-sibling::span");

        // Wait for the rating elements to be visible before proceeding
        waitUntilElementVisible(productRatingBy, 20);

        // Find all elements containing product rating followed by review count
        List<WebElement> ratingReveiws = driver.findElements(productRatingBy);

        // Extract review count for each product and get the top 5 products with the
        // highest review count
        List<Integer> top5ProductReviewsCount = getTop5ProductBasedOnReviews(ratingReveiws);

        // Print the title and image url of top 5 product which has maximum number of
        // reviews
        printTop5ProductsDetailsBasedOnReviews(top5ProductReviewsCount);
    }

    // Helper or wrapper Methods for the test cases

    // Method to enter text into a text box
    private boolean enterInTextBox(WebDriver driver, WebElement inputBox, String keysToSend) {
        try {
            // Wait for the input box to be clickable
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
            wait.until(ExpectedConditions.elementToBeClickable(inputBox));
            // Clear the input box
            inputBox.clear();
            // Send keys to the input box
            inputBox.sendKeys(keysToSend);
            return true;
        } catch (Exception e) {
            System.out.println("Input box is not clickable.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Method to wait until element is visible
    private void waitUntilElementVisible(By by, int timeOutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOutSeconds));
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    // Method to apply filter
    private void applyFilter(String filterName) {
        By popularityFilterBy = By.xpath("//div[normalize-space()='" + filterName + "']");
        waitUntilElementVisible(popularityFilterBy, 20);
        WebElement element = driver.findElement(popularityFilterBy);
        element.click();
    }

    // Method to print number of products with rating greater than or equal to given
    // rating
    private void printNumberOfProductRatingGraterThanEqual(double rating) {
        By productRatingBy = By.xpath("//span[starts-with(@id, 'productRating')]/div");
        List<WebElement> productRatings = driver.findElements(productRatingBy);
        List<Double> ratingDoubles = new ArrayList<>();
        for (WebElement ele : productRatings) {
            try {
                // Get the rating text
                String ratingString = ele.getText();
                // Convert rating text to a decimal number
                double ratingValue = Double.parseDouble(ratingString);
                // Check if the rating is greater than or equal to given rating
                if (ratingValue >= rating) {
                    ratingDoubles.add(ratingValue);
                }
            } catch (StaleElementReferenceException e) {
                // Handle StaleElementReferenceException gracefully
                System.out.println("Stale element encountered. Retrying...");
            }
        }
        System.out.println("Total number of products on the page : " + productRatings.size());
        System.out.println("Total number of products having rating greater than equal to " + rating + " : "
                + ratingDoubles.size());
    }

    // Method to verify filter applied successfully
    private boolean verifyFilterApplySuccess(String filterName) {
        WebElement element = driver.findElement(By.xpath("//div[@class='_10UF8M _3LsR0e']"));
        return element.getText().equals(filterName);
    }

    // Method to print title of discounted products
    private void printTitleOfdiscountedProduct(List<WebElement> totalElements, int discount) {
        System.out.println("Total products : " + totalElements.size());
        int discountedProductCount = 0;
        for (WebElement element : totalElements) {
            String discountText = element.getText();
            int discountPercentage = Integer.parseInt(discountText.replaceAll("[^\\d]", ""));
            if (discountPercentage > discount) {
                try {
                    WebElement productContainer = element.findElement(
                            By.xpath("ancestor::div[starts-with(@data-id, 'MOB')]//div[@class='_4rR01T']"));
                    String productTitle = productContainer.getText();
                    System.out.println("Product Title: " + productTitle);
                    System.out.println("Product discount percent: " + discountPercentage);
                    discountedProductCount++;
                } catch (NoSuchElementException e) {
                    // Handle NoSuchElementException gracefully
                    System.out.println("Product title not found for discount element.");
                }
            }
        }
        if (discountedProductCount == 0) {
            System.out.println("No product is available at discount greater than " + discount + "% ");
        } else {
            System.out.println(
                    "Total product available at discount greater than " + discount + "% : " + discountedProductCount);
        }
    }

    // Method to get top 5 products based on reviews
    private List<Integer> getTop5ProductBasedOnReviews(List<WebElement> productReviewsList) {
        // list to store ratings reviews Count
        List<String> reviewsListStr = new ArrayList<>();
        // Iterate through rating elements and extract reviews Count
        for (WebElement ratingElement : productReviewsList) {
            String ratingText = ratingElement.getText();
            reviewsListStr.add(ratingText);
        }
        // Function to extract rating and count from a string
        List<Integer> ratings = new ArrayList<>();
        // Iterate through rating strings
        for (String rating : reviewsListStr) {
            // Remove parentheses and commas
            String numStr = rating.replaceAll("[^\\d]", "");
            // Parse the string to an integer
            int num = Integer.parseInt(numStr);
            ratings.add(num);
            reviewsMap.put(num, rating);
        }
        // Sort numerical values in descending order
        Collections.sort(ratings, Collections.reverseOrder());
        // Take the top 5 numerical values
        return ratings.subList(0, Math.min(ratings.size(), 5));
    }

    // Method to print details of top 5 products based on reviews
    private void printTop5ProductsDetailsBasedOnReviews(List<Integer> products) {
        for (int review : products) {
            String spanText = reviewsMap.get(review);
            // XPath expressions targeting the title and image elements for each product
            WebElement titleElement = driver.findElement(
                    By.xpath("//span[text()='" + spanText + "']/ancestor::div[@data-id]//a[@class='s1Q9rs']"));
            String title = titleElement.getAttribute("title");
            WebElement imageElement = driver.findElement(
                    By.xpath("//span[text()='" + spanText + "']/ancestor::div[@data-id]//div[@class='CXW8mj']/img"));
            String imageUrl = imageElement.getAttribute("src");
            // Print title and image URL
            System.out.println("Element Rating : " + spanText);
            System.out.println("Title: " + title);
            System.out.println("Image URL: " + imageUrl);
        }
    }
}
