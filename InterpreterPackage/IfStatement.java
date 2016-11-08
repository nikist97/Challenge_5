package InterpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class IfStatement {

    private ArrayList<String> ifSource;
    private ArrayList<Token> variables;
    private ArrayList<Method> methods;
    private ArrayList<String> possibleCommands;

    public IfStatement(ArrayList<String> ifsource, ArrayList<Token> variables,ArrayList<Method> methods){
        this.ifSource = ifsource;
        this.variables = variables;
        this.methods = methods;
        this.possibleCommands = new ArrayList<String>();
        this.possibleCommands.add("clear");
        this.possibleCommands.add("incr");
        this.possibleCommands.add("decr");
    }

    private ArrayList<String> getStringTokens(String statement){
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer stringTokenizer = new StringTokenizer(statement);
        while(stringTokenizer.hasMoreTokens()){
            tokens.add((stringTokenizer.nextToken()));
        }
        return tokens;
    }

    private Token createToken(String token_input){
        switch (token_input) {

            case "clear":
                return new Token("command", "clear");
            case "incr":
                return new Token("command", "incr");
            case "decr":
                return new Token("command", "decr");
            case "if":
                return new Token("if");
            case "while":
                return new Token("loop", "while");
            case "is":
                return new Token("is");
            case "not":
                return new Token("not");
            case "return":
                return new Token("return");
            case "do":
                return new Token("do");
            case "end":
                return new Token("end");
            case ":" :
                return  new Token("eol");

            default:
                if (token_input.matches("^-?\\d+$")){
                    return new Token("integer", Integer.parseInt(token_input));
                }
                else if (token_input.matches("^[a-zA-Z]+$") && !token_input.equals("return")){
                    return new Token("variable", token_input);
                }

                throw new SyntaxException("Wrong syntax");
        }

    }

    private boolean isStatement(String statement) {
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() == 2 && this.possibleCommands.contains(stringTokens.get(0)));
    }

    private boolean isWhileLoop(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("while"));
    }

    private boolean isOperator(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        if (stringTokens.size() != 0){
            Token firstToken = createToken(stringTokens.get(0));
            return firstToken.getType().equals("variable");
        }
        return false;
    }

    private boolean isIfStatement(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("if"));
    }

    private boolean isValidIfStatement(String firstLine, String lastLine){
        ArrayList<String> FirstLineTokens = this.getStringTokens(firstLine);
        ArrayList<String> LastLineTokens = this.getStringTokens(lastLine);

        //making sure the line follow the syntax rule : if variable is/not integer do :
        if (FirstLineTokens.size() != 6){return false;}
        Token ifToken = this.createToken(FirstLineTokens.get(0));
        if(!(ifToken.getType().equals("if"))){return false;}
        Token variableToken = this.createToken(FirstLineTokens.get(1));
        if (!(variableToken.getType().equals("variable"))){return false;}
        Token IsNotToken = this.createToken(FirstLineTokens.get(2));
        if (!(IsNotToken.getType().equals("not") || IsNotToken.getType().equals("is"))){return false;}
        Token integerToken = this.createToken(FirstLineTokens.get(3));
        if (!(integerToken.getType().equals("integer") || integerToken.getType().equals("variable"))){return false;}
        Token doToken = this.createToken(FirstLineTokens.get(4));
        if (!(doToken.getType().equals("do"))){return false;}
        Token eolToken = this.createToken(FirstLineTokens.get(5));
        if (!(eolToken.getType().equals("eol"))){return false;}

        boolean findVariable = false;
        for(Token testVariable : variables){
            if (testVariable.getName().equals(variableToken.getName())){
                findVariable = true;
                break;
            }
        }
        if (!findVariable){
            return false;
        }

        if(integerToken.getType().equals("variable")){
            boolean findIntegerVarialbe = false;
            for(Token testVariable : variables){
                if(testVariable.getName().equals(integerToken.getName())){
                    findIntegerVarialbe = true;
                    break;
                }
            }
            if(!findIntegerVarialbe){
                return false;
            }
        }

        //making sure last line follows the syntax rule : end
        if(!(LastLineTokens.size() == 1)){return false;}
        Token newToken = this.createToken(LastLineTokens.get(0));
        if(!(newToken.getType().equals("end"))){return false;}

        return true;
    }

    private String fixStatement(String statement){
        statement = statement.replace(":"," :");
        statement = statement.replace("="," = ");
        statement = statement.replace("+"," + ");
        statement = statement.replace("-"," - ");
        statement = statement.replace("*"," * ");
        statement = statement.replace("/"," / ");
        statement = statement.replace("%"," % ");
        return statement;
    }

    private boolean evaluateIfCondition(String firstLine){
        ArrayList<String> FirstLineTokens = this.getStringTokens(firstLine);
        Token variableToken = this.createToken(FirstLineTokens.get(1));
        for(Token token : this.variables){
            if (token.getName().equals(variableToken.getName())){
                variableToken = token;
                break;
            }
        }

        Token integerToken = this.createToken(FirstLineTokens.get(3));
        int variableValue = Integer.parseInt(variableToken.value);
        int integerValue;
        if (integerToken.getType().equals("variable")){
            for(Token token : this.variables){
                if (token.getName().equals(integerToken.getName())){
                    integerToken = token;
                    break;
                }
            }
        }
        integerValue = Integer.parseInt(integerToken.getValue());

        Token IsNotToken = this.createToken(FirstLineTokens.get(2));
        if (IsNotToken.getType().equals("not")){
            return (variableValue != integerValue);
        }
        else if (IsNotToken.getType().equals("is")){
            return (variableValue == integerValue);
        }
        throw new SyntaxException("Wrong syntax in an if statement");
    }

    public void executeIfStatement(){
        Iterator iterator = this.ifSource.iterator();

        String firstLine = (String) iterator.next();
        String lastLine = this.ifSource.get(this.ifSource.size() - 1);

        if (!isValidIfStatement(firstLine,lastLine)){
            throw new SyntaxException("Wrong syntax or variable not declared in an if statement");
        }

        boolean ifCondition = evaluateIfCondition(firstLine);

        for(int index = 0; index < this.ifSource.size(); index++){
            String line = this.ifSource.get(index);
            if (line.substring(0,3).equals("   ")){
                String new_line = line.substring(3);
                this.ifSource.set(index,new_line);
            }
        }

        String line = (String) iterator.next();

        if(ifCondition){
            while(iterator.hasNext()){
                line = fixStatement(line);

                // check for comments
                if (getStringTokens(line).size() != 0 && getStringTokens(line).get(0).startsWith("#")){
                    line = (String) iterator.next();
                    line = fixStatement(line);
                }

                if (this.isStatement(line)){
                    ArrayList<String> stringTokens = getStringTokens(line);
                    Token currentToken = this.createToken(stringTokens.get(0));
                    Token nextToken = this.createToken(stringTokens.get(1));
                    Statement statement = new Statement(currentToken, nextToken, this.variables);
                    statement.executeCommand();
                }

                else if (this.isOperator(line)){
                    Operator operator = new Operator(line, this.variables, this.methods);
                    operator.executeOperator();
                }

                else if (this.isWhileLoop(line)){
                    ArrayList<String> loopSource = new ArrayList<String>();
                    loopSource.add(line);

                    while (true){
                        line = (String) iterator.next();

                        if (line.length() == 0){
                            continue;
                        }
                        else if (this.getStringTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                            break;
                        }
                        else if (line.substring(0,3).equals("   ")){
                            loopSource.add(line);
                        }
                        else {
                            throw new SyntaxException("Wrong syntax");
                        }
                    }

                    WhileLoop loop = new WhileLoop(loopSource,this.variables,this.methods);
                    loop.executeWhileLoop();
                }

                else if (this.isIfStatement(line)){
                    ArrayList<String> ifSource = new ArrayList<String>();
                    ifSource.add(line);

                    while(true){
                        line = (String) iterator.next();

                        if (line.length() == 0){
                            continue;
                        }
                        else if (this.getStringTokens(line).get(0).equals("end") && !line.substring(0,3).equals("   ")){
                            ifSource.add(line);
                            break;
                        }
                        else if (line.substring(0,3).equals("   ")){
                            ifSource.add(line);
                        }
                        else {
                            throw new SyntaxException("Wrong syntax");
                        }
                    }

                    IfStatement ifStatement = new IfStatement(ifSource,this.variables,this.methods);
                    ifStatement.executeIfStatement();
                }

                else {
                    throw new SyntaxException("Wrong Syntax in If statement source");
                }

                if (iterator.hasNext()){
                    line = (String) iterator.next();
                }

            }
        }
    }

}
