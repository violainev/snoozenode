package org.inria.myriads.snoozenode.localcontroller.monitoring.threshold;

import java.io.*;
import java.util.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpuTemperature
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(CpuTemperature.class);
    
    public static double getTemperature()
    {        
        Runtime r = Runtime.getRuntime();
        String f, temp;
        f = "sensors -u";
        double temperature = 0.0;
        
        int nbTemp = 0;
        double total = 0;
        
        Process p;
		try
		{
			p = r.exec(f);
		
            BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        
	        while((temp = pin.readLine()) != null)
            {
                Pattern pat = Pattern.compile("temp[0-9]+_input:.*");

                Matcher m = pat.matcher(temp);
                if (m.find())
                {
                    Pattern pat2 = Pattern.compile("[0-9]+(.[0-9]+)+");
                    Matcher m2 = pat2.matcher(m.group(0));

                    if (m2.find())
                    {
                        total += Double.parseDouble(m2.group(0));
                        nbTemp++;
                    }
                }
            }
        }
		catch (IOException e)
        {
			e.printStackTrace();
		}
		temperature = total / nbTemp;

        return temperature;
    }
}