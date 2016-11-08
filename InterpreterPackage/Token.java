package InterpreterPackage;

public class Token {

    // type = command,variable,integer
    private String type;
    public String value;
    private String name;

    //constructor for command, variable, loop, operators, methods
    public Token(String type, String value){
        this.type = type;
        if (this.type.equals("command") || this.type.equals("loop") || this.type.equals("operator") || this.type.equals("method")){
            this.value = value;
        }
        else if (this.type.equals("variable")){
            this.name = value;
        }
    }


    //constructor for keywords like do,not,end,if,:
    public Token(String type){
        this.type = type;
    }

    //constructor for integer
    public Token(String type, int value){
        this.type = type;
        this.value = Integer.toString(value);
    }

    public String getType(){
        return this.type;
    }

    public String getValue(){
        return this.value;
    }

    public String getName(){
        return this.name;
    }

}
