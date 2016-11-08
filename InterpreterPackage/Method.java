package InterpreterPackage;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class Method {

    private ArrayList<String> methodSource;
    private ArrayList<Token> localVariables;
    private ArrayList<Token> variables;
    private int numberOfArguments;
    public String methodName;
    private ArrayList<Method> methods;
    private ArrayList<String> possibleCommands;

    public Method(ArrayList<String> methodSource, ArrayList<Method> methods, ArrayList<Token> variables){
        this.methodSource = methodSource;
        this.methods = methods;
        this.numberOfArguments = 0;
        this.localVariables = new ArrayList<Token>();
        this.variables = variables;
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
            case "do":
                return new Token("do");
            case "define":
                return new Token("define");
            case "return":
                return new Token("return");
            case "end":
                return new Token("end");
            case ":" :
                return  new Token("eol");

            default:
                if (token_input.matches("^-?\\d+$")){
                    return new Token("integer", Integer.parseInt(token_input));
                }
                else if (token_input.matches("^[A-Z][a-zA-Z]+$")){
                    return new Token("method", token_input);
                }
                else if (token_input.matches("^[a-zA-Z]+$") && !token_input.equals("return")){
                    return new Token("variable", token_input);
                }

                throw new SyntaxException("Wrong syntax");
        }

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

    private boolean isReturnStatement(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        if(stringTokens.size() != 2){return false;}
        Token returnToken = this.createToken(stringTokens.get(0));
        Token variable = this.createToken(stringTokens.get(1));
        return (returnToken.getType().equals("return") && variable.getType().equals("variable"));
    }

    private boolean isStatement(String statement) {
        ArrayList<String> stringTokens = getStringTokens(statement);
        return stringTokens.size() == 2 && this.possibleCommands.contains(stringTokens.get(0));
    }

    private boolean isIfStatement(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("if"));
    }

    private boolean isOperator(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        if (stringTokens.size() != 0){
            Token firstToken = createToken(stringTokens.get(0));
            return firstToken.getType().equals("variable");
        }
        return false;
    }

    private boolean isWhileLoop(String statement){
        ArrayList<String> stringTokens = getStringTokens(statement);
        return (stringTokens.size() != 0 && stringTokens.get(0).equals("while"));
    }

    private boolean isValidMethodCreation(){
        if (!(this.methodSource.size() > 2)){
            return false;
        }

        String firstLine = this.methodSource.get(0);
        firstLine = fixStatement(firstLine);
        String lastLine = this.methodSource.get(this.methodSource.size() - 1);

        ArrayList<String> stringTokens = this.getStringTokens(firstLine);
        Token defToken = this.createToken(stringTokens.get(0));
        Token methodNameToken = this.createToken(stringTokens.get(1));
        Token endToken = this.createToken(stringTokens.get(stringTokens.size()-1));

        if (!(defToken.getType().equals("define"))){return false;}
        if (!(methodNameToken.getType().equals("method"))){return false;}
        this.methodName = methodNameToken.getValue();
        if(!(endToken.getType().equals("eol"))){return false;}

        if (stringTokens.size() > 3){
            for(int index = 2; index < stringTokens.size() - 1; index++){
                Token variable = this.createToken(stringTokens.get(index));
                if (!(variable.getType().equals("variable"))){return false;}
                numberOfArguments += 1;
                this.localVariables.add(variable);
            }
        }


        if (!(lastLine.equals("end"))){
            return false;
        }

        return true;
    }

    public void createMethod(){
        if (isValidMethodCreation()){
            this.methods.add(this);
        }
        else {
            throw new SyntaxException("Wrong syntax in method creation");
        }
    }

    private boolean isValidMethodCall(int numberOfVariables){
        if (!(this.numberOfArguments == numberOfVariables)){
            return false;
        }

        return true;
    }

    public int executeMethod(ArrayList<Token> arguments){
        if(!(isValidMethodCall(arguments.size()))){
            throw new SyntaxException("Wrong Syntax in a method call");
        }

        if(arguments.size() != 0){
            for (int index = 0; index < arguments.size(); index++){
                this.localVariables.get(index).value = arguments.get(index).getValue();
            }
        }

        for(int index = 0; index < this.methodSource.size(); index++){
            String line = this.methodSource.get(index);
            if (line.substring(0,3).equals("   ")){
                String new_line = line.substring(3);
                this.methodSource.set(index,new_line);
            }
        }

        Iterator iterator = this.methodSource.iterator();
        String line = (String) iterator.next();
        line = (String) iterator.next();

        while (iterator.hasNext()){
            line = fixStatement(line);

            if (line.equals("")){
                if (iterator.hasNext()){
                    line = (String) iterator.next();
                }
                continue;
            }

            // check for comments
            if (getStringTokens(line).size() != 0 && getStringTokens(line).get(0).startsWith("#")){
                line = (String) iterator.next();
                line = fixStatement(line);
            }

            if (this.isStatement(line)){
                ArrayList<String> stringTokens = getStringTokens(line);
                Token currentToken = this.createToken(stringTokens.get(0));
                Token nextToken = this.createToken(stringTokens.get(1));
                Statement statement = new Statement(currentToken, nextToken, this.localVariables);
                statement.executeCommand();
            }

            else if (this.isOperator(line)){
                Operator operator = new Operator(line, this.localVariables, this.methods);
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
                        throw new SyntaxException("Wrong syntax in a while loop");
                    }
                }

                WhileLoop loop = new WhileLoop(loopSource,localVariables,this.methods);
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
                        throw new SyntaxException("Wrong syntax in an If Statement");
                    }
                }
                IfStatement ifStatement = new IfStatement(ifSource,localVariables,this.methods);
                ifStatement.executeIfStatement();
            }

            else if (this.isReturnStatement(line)){
                ArrayList<String> stringTokens = getStringTokens(line);
                Token variable = this.createToken(stringTokens.get(1));
                boolean findVariable = false;
                for(Token testVariable : this.localVariables){
                    if(testVariable.getName().equals(variable.getName())){
                        findVariable = true;
                        variable = testVariable;
                        break;
                    }
                }

                if(!findVariable){
                    throw new SyntaxException("Variable, which was not declared, is used in an return statement.");
                }

                return Integer.parseInt(variable.getValue());

            }

            if (iterator.hasNext()){
                line = (String) iterator.next();
            }

        }

        throw new SyntaxException("No return statement in a method.");
    }
}
