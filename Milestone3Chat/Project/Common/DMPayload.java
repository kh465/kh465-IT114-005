//kh465 11/22/24
package Project.Common;

import java.util.ArrayList;

public class DMPayload extends Payload
{
    private String recipientClientName;
    private long recipientClientId;
    private ArrayList<Long> mutedClients = new ArrayList<Long>();

    public String getRecClientName()
    {
        return recipientClientName;
    }

    public void setRecClientName(String recipientClientName)
    {
        this.recipientClientName = recipientClientName;
    }

    public long getRecClientId()
    {
        return recipientClientId;
    }

    public void setRecClientId(long recipientClientId)
    {
        this.recipientClientId = recipientClientId;
    }

    public void setMutedClients(ArrayList<Long> clientsToMute)
    {
        mutedClients = clientsToMute;
    }

    public ArrayList<Long> getMutedClients()
    {
        return mutedClients;
    }
}
