//kh465 11/13/24
package Project.Server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextMarkup
{
    private static final Pattern[] MARKUP = 
    {
        Pattern.compile("\\*\\*(.*?)\\*\\*"), //bold
        Pattern.compile("\\*(.*?)\\*"), //italic
        Pattern.compile("_(.*?)_"), //underline
        Pattern.compile("#r(.*?)r#"), //red
        Pattern.compile("#g(.*?)g#"), //green
        Pattern.compile("#b(.*?)b#") //blue
    };
    private static final String[] MARKUP_REPLACE = 
    {
        "<b>$1</b>", //bold
        "<i>$1</i>", //italic
        "<u>$1</u>", //underline
        "<span style=\"color: #FF0000\">$1</span>", //red
        "<span style=\"color: #00FF00\">$1</span>", //green
        "<span style=\"color: #0000FF\">$1</span>" //blue
    };
    public String TMFormat(String message)
    {
        for (int i = 0; i < MARKUP.length; i++)
        {
            Matcher matcher = MARKUP[i].matcher(message);
            if (matcher.find())
                message = matcher.replaceAll(MARKUP_REPLACE[i]);
        }
        return message;
    }
}
