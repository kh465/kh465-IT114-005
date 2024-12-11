//kh465 11/13/24
package Project.Common;

public class RollPayload extends Payload
{
    private int arg1, arg2;

    public RollPayload(int arg1, int arg2)
    {
        setPayloadType(PayloadType.ROLL);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public int getArg1()
    {
        return arg1;
    }

    public int getArg2()
    {
        return arg2;
    }

    public void setArg1(int arg1)
    {
        this.arg1 = arg1;
    }

    public void setArg2(int arg2)
    {
        this.arg2 = arg2;
    }

    @Override
    public String toString()
    {
        return super.toString() + String.format(" (%sd%s)", getArg1(), getArg2());
    }
}
