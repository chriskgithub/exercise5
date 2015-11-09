
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TemplateEngine - process templates where the variables are specified as ${varname}
 * and the data for the variables is in a Map, with the key being the varname and value 
 * being what to replace it with. If the variable is wrapped in the start and stop strings,
 * preserve the outer ones so ${${varname}} goes to ${value} on expansion
 */

public class TemplateEngine
{
    private Map<String,String> values = null;    // the key/value pairs to use in the template
    public static final String VARIABLE_START = "${";
    public static final int VSLEN = VARIABLE_START.length();
    public static final String VARIABLE_END = "}";
    public static final int VELEN = VARIABLE_END.length();


    /**
     * main - used only for unit testing
     * This was done with Test Driven Development and the tests were created before
     * the code that made them work correctly
     */
    public static void main(String[] args) throws Exception
    {
        // test 1, null map
        try {
            TemplateEngine t1 = new TemplateEngine(null);
            assert(false);
        }
        catch(Exception x1) {
            // expected behavior
            System.out.println("Test 1 Success");
        }

        // test 2, null template
        try {
            TemplateEngine t2 = new TemplateEngine(new HashMap<String,String>());
            String s2 = t2.processTemplate(null);
            assert(false);
        }
        catch(Exception x2) {
            // expected behavior
            System.out.println("Test 2 Success");
        }

        // test 3, template with no variables
        String template3 = "This has no variables";
        TemplateEngine t3 = new TemplateEngine(new HashMap<String,String>());
        String ans3 = t3.processTemplate(template3);
        assert(template3.equals(ans3));
        System.out.println("Test 3 Success");

        // test 4, one simple variable
        String template4 = "This has ${one} variable";
        HashMap<String,String> map4 = new HashMap<>();
        map4.put("one","1");
        TemplateEngine t4 = new TemplateEngine(map4);
        String ans4 = t4.processTemplate(template4);
        assert(ans4.indexOf("1") != -1);
        assert(ans4.indexOf("one") == -1);
        System.out.println("Test 4 Success");

        // test 5, multiple variables also tests the variable end as the last character in the template
        String template5 = "This rain in ${country} falls mainly in the ${location}";
        HashMap<String,String> map5 = new HashMap<>();
        map5.put("country","Spain");
        map5.put("location","plain");
        TemplateEngine t5 = new TemplateEngine(map5);
        String ans5 = t5.processTemplate(template5);
        assert(ans5.indexOf("Spain") != -1);
        assert(ans5.indexOf("plain") != -1);
        System.out.println("Test 5 Success");

        // test 6, unterminated variable
        HashMap<String,String> map6 = new HashMap<>();
        map6.put("country","Spain");
        String template6 = "This rain in ${country falls mainly in the street";
        try {
            TemplateEngine t6 = new TemplateEngine(map6);
            String s6 = t6.processTemplate(template6);
            assert(false);
        }
        catch(Exception x6) {
            // expected behavior
            System.out.println("Test 6 Success");
        }

        // test 7, escaped variable
        String template7 = "This rain in ${${country}} falls mainly on the ${location}";
        HashMap<String,String> map7 = new HashMap<>();
        map7.put("country","Spain");
        map7.put("location","roof");
        TemplateEngine t7 = new TemplateEngine(map7);
        String ans7 = t7.processTemplate(template7);
        assert(ans7.indexOf("${Spain}") != -1);
        assert(ans7.indexOf("roof") != -1);
        System.out.println("Test 7 Success");

        // test 8, all the delimiter characters, but not forming a variable substitution
        String template8 = "This rain in $ { $ {country}} falls mainly on the $ {location}";
        HashMap<String,String> map8 = new HashMap<>();
        map8.put("country","France");
        map8.put("location","grapes");
        TemplateEngine t8 = new TemplateEngine(map8);
        String ans8 = t8.processTemplate(template8);
        assert(template8.equals(ans8));
        System.out.println("Test 8 Success");
    }

    /**
     * TemplateEngine constructor
     * @param values a Map<String,String> with keys to be replaced by values
     * @throws IllegalArgumentException if values is null
     */
    public TemplateEngine(Map<String,String> values)
    {
        super();
        if(values == null)
        {
            throw new IllegalArgumentException("Map of replacement values must not be null");
        }
        this.values = values;
    }

    /**
     * processTemplate - replace the variables in the template with the values from the map
     * @param template the String template to expand
     * @return the expanded template as a String
     * @throws IllegalArgumentException on a null template or there is a non-terminated variable 
     * @throws Exception if a variable is used that isn't in the map
     * NOTE: This could be done with StringTokenizer, but that is deprecated and it could be done with 
     * String.split, but getting the delimiters from that involves lookarounds and results in unclear code.
     */
    public String processTemplate(String template) throws Exception
    {
        if(template == null)
        {
            throw new IllegalArgumentException("template must not be null");
        }

        StringBuilder sb1 = new StringBuilder();

        while(template.length() > 0)
        {
            int vstart = template.indexOf(VARIABLE_START);   // find the next variable start
            if(vstart == -1)
            {
                // no more variables so give back the whole thing
                sb1.append(template);
                template = "";
            }
            else {
                // figure out if the thing after the next variable start is another variable start
                boolean quoted = template.length() > (vstart + VSLEN) && VARIABLE_START.equals(template.substring(vstart+VSLEN,vstart+VSLEN+VSLEN));
                if(quoted)
                {
                    // so we put the outer variable start in the output and move up to the inner variable start
                    sb1.append(template.substring(0,vstart+VSLEN));
                    template = template.substring(vstart+VSLEN);
                    vstart = 0;
                }
                // copy everything up to the variable start
                sb1.append(template.substring(0,vstart));
                template = template.substring(vstart + VSLEN);

                // find the variable end
                int vend = template.indexOf(VARIABLE_END);
                if(vend == -1) {
                    throw new IllegalArgumentException("template has unterminated variable syntax");
                }

                // get the name of the variable to look up in the map
                String vname = template.substring(0,vend);
                String vvalue = values.get(vname);
                if(vvalue == null) {
                    throw new Exception("template has variable name not in map \"" + vname + "\"");
                }
                sb1.append(vvalue);  // put in the value we found
                quoted = false;
                template = template.substring(vend + VELEN);  // move to first character after the variable end
            }
        }

        return sb1.toString();
    }

}
