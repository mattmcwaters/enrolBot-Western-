/**
 * Created by Matt on 2015-08-01.
 */
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

import org.apache.commons.logging.LogFactory;

import sun.plugin.javascript.navig.Anchor;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class AshleyBot {
    public static void main(String[] args) throws Exception{
        int classes=0;
        int successCount=0;
        int attemptCount=0;
        int waitTime=-1;
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(
            Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        WebClient wc = new WebClient();
        wc.getOptions().setThrowExceptionOnScriptError(false);

        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        wc.setAjaxController(new AjaxController(){
            @Override
            public boolean processSynchron(HtmlPage page, WebRequest request, boolean async)
            {
                return true;
            }
        });
        Scanner in = new Scanner(System.in);





        System.out.println("Hi! I will enrol in all of your classes for you, just sit back and relax :)");
        System.out.println("But first, how long should I wait between each attempt? (in minutes)");
        while(waitTime==-1){
            try{
                waitTime= Integer.parseInt(in.nextLine());

            }
            catch(Exception e){
                System.out.println("Please enter a number!");
            }
        }

        while(true){
            attemptCount++;


            HtmlPage uwoMain = wc.getPage("https://student.uwo.ca");


            HtmlSubmitInput loginBtn = uwoMain.getFirstByXPath("//input[@value='Sign In']");
            HtmlTextInput uname = uwoMain.getFirstByXPath("//input[@id='userid']");
            HtmlPasswordInput pword = uwoMain.getFirstByXPath("//input[@id='pwd']");
            
            //Removed these lines due to them containing my family member's login information		
            //uname.setValueAttribute("USERNAME");
            //pword.setValueAttribute("PASSWORD");

            HtmlPage loggedIn = loginBtn.click();
            HtmlPage detailsGot = wc.getPage(
                "https://student.uwo.ca/psc/heprdweb/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES.SSR_SSENRL_APPT.GBL?Page=SSR_SSENRL_APPT&amp;Action=A&amp;ExactKeys=Y&amp;ACAD_CAREER=UGRD&amp;EMPLID=250778787&amp;INSTITUTION=UWO&amp;STRM=1159&amp;TargetFrameName=None");
            wc.waitForBackgroundJavaScriptStartingBefore(5000);


            List<HtmlAnchor> anchors = detailsGot.getAnchors();
            HtmlAnchor plannerAnchor = anchors.get(3);
            HtmlPage plannerPage = plannerAnchor.click();



            HtmlAnchor workSheetAnchor = plannerPage.getAnchors().get(7);

            HtmlPage worksheetPage = workSheetAnchor.click();
            wc.waitForBackgroundJavaScript(1000);

            List<HtmlAnchor> anchorList = worksheetPage.getAnchors();
            List hiddenInputs = worksheetPage.getByXPath("//input[@type='hidden']");
            for(Object o: hiddenInputs){
                if(((HtmlHiddenInput)o).getNameAttribute().startsWith("P_SELECT")){
                    ((HtmlHiddenInput)o).setValueAttribute("Y");
                    classes++;
                }
            }
            System.out.println("Attempting to enrol in " + classes +" classes. This is attempt " + attemptCount + " with "+ successCount + " successes up to date...");
                                                                                                                                                                        wc.waitForBackgroundJavaScript(500);

            HtmlPage finalPage = (HtmlPage) worksheetPage.executeJavaScript("javascript:submitAction_win0(document.win0,'DERIVED_REGFRM1_LINK_ADD_ENRL$115$');").getNewPage();
            JavaScriptJobManager manager = worksheetPage.getEnclosingWindow().getJobManager();


            wc.waitForBackgroundJavaScript(5000);
            anchorList = finalPage.getAnchors();
            //Working up until here
            HtmlAnchor finalButton = finalPage.getAnchorByText("Finish Enrolling");
            //String test = worksheetPage.executeJavaScript("javascript:submitAction_win0(document.win0,'DERIVED_REGFRM1_SSR_PB_SUBMIT');").toString();
            finalPage = (HtmlPage) worksheetPage.executeJavaScript("javascript:submitAction_win0(document.win0,'DERIVED_REGFRM1_SSR_PB_SUBMIT');").getNewPage();
            wc.waitForBackgroundJavaScript(5000);
            finalPage = finalButton.click();
            wc.waitForBackgroundJavaScript(5000);
            anchorList = finalPage.getAnchors();
            List success = finalPage.getByXPath("//img[@class='SSSIMAGECENTER']");
            if(success!=null){
                for(Object o: success){
                    if(((HtmlImage)o).getAltAttribute().equals("Success")){
                        System.out.println("Successfully enrolled in a class!");
                         successCount+=1;
                    }
                }
                if(successCount==classes){
                    return;
                }
                //System.out.println("Would you like to continue?");
                //String s=in.nextLine();
                //if(s.equals("n")) {
                //    return;
                //}
            }
            classes=0;
            if(waitTime==1){
                System.out.println("Waiting for " + waitTime + " minute");

            }
            else{
                System.out.println("Waiting for " + waitTime + " minutes");
            }
            TimeUnit.MINUTES.sleep(waitTime);
        }
    }
}
