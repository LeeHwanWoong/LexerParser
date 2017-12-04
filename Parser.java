import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.

    Token token;          // current token from the input stream
    Lexer lexer;

    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }

    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }

    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok
                           + "; saw: " + token);
        System.exit(1);
    }

    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok
                           + "; saw: " + token);
        System.exit(1);
    }

    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        Declarations d = declarations();
        Block b = statements();
        // program함수의 경우 Program을 리턴한다. 이때 Program은 Declarations변수와 Block변수를 파라미터로 갖는다. 따라서 각각 하나씩 변수로 만들어주고 각각읠 declarations, statements함수를 이용해서 구조화 해준다.
        match(TokenType.RightBrace);
        return new Program(d,b);  // 완성된 Declarations, Block변수를 파라미터로 한 새로운 Program변수를 만들어 리턴해준다.
    }

    private Declarations declarations () {
        // Declarations --> { Declaration }
        Declarations d = new Declarations();
        while(isType()){
        	declaration(d);
        }
        return d;  //여러개의 declaration들을 declarations로 묶어서 리턴한다.
    }

    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        Type type = type();
        Declaration d = new Declaration(match(TokenType.Identifier), type);
        ds.add(d);		//ds에 새로운 declaration 삽입
        while(token.type() == TokenType.Comma){
        	match(TokenType.Comma);
        	Declaration dT = new Declaration(match(TokenType.Identifier),type);
        	ds.add(dT);
        }
        match(TokenType.Semicolon);
        // 여러개의 선언문을 인식하는 함수이다. 하나의 타입에 여러 변수를 생성할 수 있으므로 다음 토큰이 ','인 경우 같은 타입으로 변수 선언을 했음을 인식해준다. 또한 이렇게 완성한 Declaration을 Declarations에 추가해준다.
    }

    private Type type () {
        // Type  -->  int | bool | float | char
        Type t;
        if(token == Token.intTok){
        	t = new Type("int");
        	match(TokenType.Int);
        }
        else if(token == Token.floatTok){
        	t = new Type("float");
        	match(TokenType.Float);
        }
        else if(token == Token.boolTok){
        	t = new Type("bool");
        	match(TokenType.Bool);
        }
        else{
        	t = new Type("char");
        	match(TokenType.Char);
        }
        // 변수의 타입을 인식하는 함수이다. Type변수를 만들어서 다음 토큰을 인식하고 각 토큰에 따라 각 타입을 할당해준다.
        return t;
    }

    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        while(!token.type().equals(TokenType.RightBrace)){
        	b.members.add(statement());
        }
        // 선언문 다음으로 statement들을 구조화하는 함수로 Block을 리턴해준다. 또한 프로그램 종료를 알리는 '}'기호를 만나기 전까지 계속해서 statement들을 입력받아 추가한다.
        return b;
    }

    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = new Skip();
        if(token.type().equals(TokenType.If)){
        	s = ifStatement();
        }
        else if(token.type().equals(TokenType.Identifier)){
        	s = assignment();
        }
        else if(token.type().equals(TokenType.While)){
        	s = whileStatement();
        }
        else if(token.type().equals(TokenType.LeftBrace)){
        	match(TokenType.LeftBrace);
        	s = statement();
        	match(TokenType.RightBrace);
        }
        else{
        	match(TokenType.Semicolon);
        }
        //각각 다음 토큰이 '{', 'ID', 'while', 'if',';'일 경우 각각 block, assignment, while문, if문, 세미콜론으로 인식을 해서 각각을 구조화해주는 함수를 발동시켜준다.
        return s;
    }

    private Assignment assignment () {
    	Variable target = new Variable(match(TokenType.Identifier));
    	match(TokenType.Assign);
    	Expression source = expression();
    	match(TokenType.Semicolon);
        // Assignment --> Identifier = Expression ;
        return new Assignment(target,source);  //assignment의 경우 id = expression형태이므로 id를 인식해서 이를 Variable변수에 넣고 '='뒤에 오는 표현식을 expression함수를 이용해서 구조화한다. 이렇게 만든 Variable과 Expression 변수를 파라미터로 하는 Assignment를 새로 만들어 리턴해준다.
    }

    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
        match(TokenType.If);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        match(TokenType.LeftBrace);
        Statement s = statements();
        match(TokenType.RightBrace);

        if(token.type().equals(TokenType.Else)){
        	match(TokenType.Else);
        	match(TokenType.LeftBrace);
        	Statement st = statements();
        	match(TokenType.RightBrace);
        	return new Conditional(e,s,st);
        }
        else{
        	return new Conditional(e,s);
        }
          //if문의 경우 차례로 'if', '('를 인식하고 표현식을 expression함수를 통해 구조화 하고 ')'를 인식한 뒤 실행해야 할 statement를 statements를 통해 구조화한다. 또한 else가 있을 수 있으므로 다음 토큰이 else인지 확인하고 else인경우 다음 statement를 다시한번 구조화하고 이를 파라미터로 해서 Conditional에 대입해준다.
    }

    private Loop whileStatement () {
        // WhileStatement --> while ( Expression ) Statement
        match(TokenType.While);
        match(TokenType.LeftParen);
        Expression e = expression();
        match(TokenType.RightParen);
        match(TokenType.LeftBrace);
        Statement s = statements();
        match(TokenType.RightBrace);
        return new Loop(e,s);  // 순서대로 while, '(' 을 입력받고 expression을 하나 구조화하고 ')'를 인식하고 statements를 통해 statement들을 인식받는다.
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
        Expression e = conjunction();
        if(token.type().equals(TokenType.Or)){
        	Operator op = new Operator("||");
        	match(TokenType.Or);
        	Expression et = conjunction();
        	Binary b = new Binary(op,e,et);
        	while(token.type().equals(TokenType.Or)){
        		match(TokenType.Or);
	        	Expression ett = conjunction();
	        	Binary bt = b;
	        	b = new Binary(op,bt,ett);
        	}
        	return b;
        }
        else{
        	return e;
        }
        // expression은 Conjunction이 여러개 나올 수 있다. 하나는 무조건 나오므로 이를 conjunction함수로 구조화하고 다음 토큰이 '||'일 경우 새로운 conjunction함수를 통해 이를 추가해준다. '||'가 나오지 않을때까지 반복한다.
    }

    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        Expression e = equality();
        if(token.type().equals(TokenType.And)){
        	Operator op = new Operator("&&");
        	match(TokenType.And);
        	Expression et = equality();
        	Binary b = new Binary(op,e,et);
        	while(token.type().equals(TokenType.And)){
        		match(TokenType.And);
        		Expression ett = equality();
        		Binary bt = b;
        		b = new Binary(op,bt,ett);
        	}
        	return b;
        }
        else{
        	return e;
        }
        // conjunction은 Equality가 여러개 나올 수 있다. 하나는 무조건 나오므로 이를 equality함수로 구조화하고 다음 토큰이 '&&'일 경우 새로운 equality함수를 통해 이를 추가해준다. '&&'가 나오지 않을때까지 반복한다.
    }

    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        Expression e = relation();
        while(isEqualityOp()){
        	Operator op = new Operator(match(token.type()));
        	match(token.type());
        	Expression et = relation();
        	e = new Binary(op,e,et);
        }
        return e;  // 하나의 Realation을 먼저 relation함수를 이용해서 구조화 하고, 다음 토큰이 '==', '!='인지 확인하고 맞는 경우 다음 relation을 구조화한다.
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition]
        Expression e = addition();
        while(isRelationalOp()){
        	Operator op = new Operator(match(token.type()));
        	if(token.type().equals(TokenType.Assign)){
        		match(token.type());
        	}
        	Expression et = addition();
        	e = new Binary(op,e,et);
        }
        return e;  // 하나의 Addition을 먼저 addition함수를 이용해서 구조화 하고, 다음 토큰이 '>', '<', '<=', '>=' 인지 확인해서 맞는 경우 다음 Addition을 구조화한다.
    }

    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }

    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }

    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
       	Value v;
    	if(token.type().equals(TokenType.IntLiteral)){
    		int s = Integer.parseInt(match(TokenType.IntLiteral));
    		//토큰이 int형일 경우 이를 int형으로 바꿔 저장해주고
    		v = new IntValue(s);
    		//이를 다시 IntValue로 만들어줘서 리턴한다.
    		return v;
    	}
    	else if(token.type().equals(TokenType.FloatLiteral)){
    		float s = Float.parseFloat(match(TokenType.FloatLiteral));
    		//토큰이 float형일 경우 이를 float형으로 바꿔 저장해주고
    		v = new FloatValue(s);
    		//이를 다시 FloatValue로 만들어줘서 리턴한다.
    		return v;
    	}
    	else if(token.type().equals(TokenType.CharLiteral)){
    		String t = match(TokenType.CharLiteral);
    		char s = t.charAt(0);
    		//char형일 경우 스트링을 받아 이 스트링의 첫번째 char를 받아서 저장해주고
    		v = new CharValue(s);
    		//이를 CharValue로 만들어줘서 리턴한다.
    		return v;
    	}
    	else{
    		if(token.type().equals(TokenType.True)){
    			v = new BoolValue(true);
    			//다음 토큰이 True일 경우 true를 BoolValue파라미터로 넣어서 리턴한다.
    			return v;
    		}
    		else{
    			v = new BoolValue(false);
    			//다음 토큰이 False일 경우 false를 BoolValue파라미터로 넣어서 리턴한다.
    			return v;
    		}
    	}
    }


    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }

    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }

    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }

    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }

    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) ||
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }

    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool)
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }

    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }

    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // display abstract syntax tree
    } //main

} // Parser
