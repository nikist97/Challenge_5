package InterpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import java.util.ArrayList;

public class Statement {

    private Token command;
    private Token variable;
    private ArrayList<Token> variables;

    public Statement(Token command, Token variable, ArrayList<Token> variables) {
        this.command = command;
        this.variable = variable;
        this.variables = variables;
    }

    private boolean valueExists(){
        boolean ValueExists = false;
        for(Token testVariable : this.variables){
            if (testVariable.getName().equals(this.variable.getName())){
                ValueExists = true;
                break;
            }
        }
        return ValueExists;
    }

    public void executeCommand(){
        if(command.getType().equals("command") && variable.getType().equals("variable")){

            switch (command.getValue()){
                case "clear" :
                    if (!this.valueExists()){
                        this.variable.value = Integer.toString(0);
                        this.variables.add(this.variable);
                    }
                    else {
                        for(int index = 0; index < this.variables.size(); index++){
                            if(this.variable.getName().equals(variables.get(index).getName())){
                                this.variables.get(index).value = Integer.toString(0);
                            }
                        }
                    }
                    break;

                case "incr" :
                    if (!this.valueExists()){
                        throw new SyntaxException("Variable " + variable.getName() + " isn't declared!");
                    }
                    else {
                        for(int index = 0; index < this.variables.size(); index++){
                            if(this.variable.getName().equals(variables.get(index).getName())){
                                int currentValue = Integer.parseInt(this.variables.get(index).value);
                                currentValue += 1;
                                this.variables.get(index).value = Integer.toString(currentValue);
                            }
                        }
                    }
                    break;

                case "decr" :
                    if (!this.valueExists()){
                        throw new SyntaxException("Variable " + variable.getName() + " isn't declared!");
                    }
                    else {
                        for(int index = 0; index < this.variables.size(); index++){
                            if(this.variable.getName().equals(variables.get(index).getName())){
                                int currentValue = Integer.parseInt(this.variables.get(index).value);
                                currentValue -= 1;
                                this.variables.get(index).value = Integer.toString(currentValue);
                            }
                        }
                    }
                    break;

                default :
                    throw new SyntaxException("Wrong syntax");
            }
        }
        else {
            throw new SyntaxException("Wrong syntax");
        }

    }

}