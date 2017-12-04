// Abstract syntax for the language C++Lite,
// exactly as it appears in Appendix B.
import java.util.*;

class Indenter {
   public int level;// 트리의 레벨
   public Indenter(int nextlevel) { level = nextlevel; }    //indenter 생성자

   public void display(String message) {
      String tab = "";
      System.out.println();
      for (int i=0; i<level; i++)
          tab = tab + "  ";
      System.out.print(tab+ level+ "."+ message); //현재 레벨의 메시지를 출력해주는 함수, 레벨당 tab을 해서 구별할 수 있게 해준다. 트리 출력을 위한 클래스
   }
}


class Program {// Program = Declarations decpart ; Block body
    Declarations decpart;
    Block body;
    Program (Declarations d, Block b) {
        decpart = d;
        body = b;
    }//program 클래스 생성자
    public void display () {
        int level = 0;
        Indenter indent = new Indenter(level);
        indent.display("Program (abstract syntax): ");
            decpart.display(level+1);
            body.display(level+1);
        System.out.println();
    }
}

class Declarations extends ArrayList<Declaration> {
    // Declarations = Declaration*

    public void display (int level) {
        Indenter indent = new Indenter(level);
        indent.display("Declarations = { ");
        String sep = "";
        for (Declaration dcl : this) {
            System.out.print(sep);
            dcl.display();
            sep = " | ";
        }
        System.out.print(" }");
    }
}//선언부 출력하는 부분

class Declaration {
    // Declaration = Variable v; Type t
    Variable v;
    Type t;

    Declaration( ) { }
    Declaration (String id, Type type) {
        v = new Variable(id); t = type;
    } // declaration  생성자

    public void display () {
       System.out.print("<" + v + ", " + t.getId() + ">");
    }
}//선언부 출력하는 부분

class Type {
    // Type = int | bool | char | float
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type FLOAT = new Type("float");
    final static Type CHAR = new Type("char");
    final static Type VOID = new Type("void");
    final static Type UNDEFINED = new Type("undefined");
    final static Type UNUSED = new Type("unused");

    protected String id;
    protected Type (String t) { id = t; }
    public String getId ( ) { return id; }
}//Type 클래스

abstract class Statement {
    // Statement = Skip | Block | Assignment | Conditional | Loop
    public void display (int level) {
         Indenter indent = new Indenter(level);
         indent.display(getClass().toString().substring(6) + ": ");
    }
}//Statement 클래스 , 저장해야할 변수가 없으므로 추상 클래스로 선언한다.
//Skip, Block, Assignment, Conditional, Loop 클래스는 Statement상속
class Skip extends Statement {
    public void display (int level) {
       super.display(level);
    }
}// Skip , ;

class Block extends Statement {
    // Block = Statement*
    public ArrayList<Statement> members = new ArrayList<Statement>();

    public void display(int level) {
        super.display(level);
        for (Statement s : members)
            s.display(level+1);
    }
}// Block,  { }

class Assignment extends Statement {
    // Assignment = Variable target; Expression source
    Variable target;
    Expression source;

    Assignment (Variable t, Expression e) {
        target = t;
        source = e;
    }//Assignment 생성자

    public void display (int level) {
       super.display(level);//부모클래스를 display해준다.
       target.display(level+1);
       source.display(level+1);
    }
}       //Assignment 는 Statement를 상속 받는다.

class Conditional extends Statement {
// Conditional = Expression test; Statement thenbranch, elsebranch
    Expression test;
    Statement thenbranch, elsebranch;


    Conditional (Expression t, Statement tp) {
        test = t; thenbranch = tp; elsebranch = new Skip( );
    } //Conditional 생성자, elsebranch가 없을 경우 Skip객체를 생성한다.

    Conditional (Expression t, Statement tp, Statement ep) {
        test = t; thenbranch = tp; elsebranch = ep;
    } //Conditional 생성자

    public void display (int level) {
       super.display(level);
       test.display(level+1);
       thenbranch.display(level+1);
       assert elsebranch != null : "else branch cannot be null";
       elsebranch.display(level+1); // 출력부분
    }
}

class Loop extends Statement {
// Loop = Expression test; Statement body
    Expression test;
    Statement body;

    Loop (Expression t, Statement b) {
        test = t; body = b; //loop생성자
    }

    public void display (int level) {
       super.display(level);
       test.display(level+1);
       body.display(level+1);
    }
} //while문 클래스 statement를 상속받는다.

abstract class Expression {
    // Expression = Variable | Value | Binary | Unary

    public void display (int level) {
         Indenter indent = new Indenter(level);
         indent.display(getClass().toString().substring(6) + ": ");
    }
} //추상 expression클래스

class Variable extends Expression {
   //Variable은 string형 아이디
    private String id;

    Variable (String s) { id = s; } //생성자

    public String id( )  { return id; }
    public String toString( ) { return id; }
    public boolean equals (Object obj) {
        String s = ((Variable) obj).id;
        return id.equals(s);
    }
    public int hashCode ( ) { return id.hashCode( ); } //해당객체의 고유한 값을 리턴해주는 hashcode 함수

    public void display (int level) {
         super.display(level);
         System.out.print(id);
    }
}//Expression을 사옥받는 Variable 클래스

abstract class Value extends Expression {
// Value = IntValue | BoolValue | CharValue | FloatValue
    protected Type type;
    protected boolean undef = true;

    int intValue ( ) {
        assert false : "should never reach here"; //assert [boolean 식] : 표현식, boolean이 참이면 프로그램을 계속돌리고 아니면 assertion error를 발생시킨다.
        return 0;
    }

    boolean boolValue ( ) {
        assert false : "should never reach here";
        return false;
    }

    char charValue ( ) {
        assert false : "should never reach here";
        return ' ';
    }

    float floatValue ( ) {
        assert false : "should never reach here";
        return 0.0f;
    }

    boolean isUndef( ) { return undef; }

    Type type ( ) { return type; }

    static Value mkValue (Type type) {
        if (type == Type.INT) return new IntValue( );
        if (type == Type.BOOL) return new BoolValue( );
        if (type == Type.CHAR) return new CharValue( );
        if (type == Type.FLOAT) return new FloatValue( ); //Type이 Int,Bool, Char, Float면 IntValue(), BoolValue(), CharValue(), FloatValue()
        throw new IllegalArgumentException("Illegal type in mkValue");
    }
} //Expression 을 상속받는 Value 클래스

class IntValue extends Value {
    private int value = 0;

    IntValue ( ) { type = Type.INT; } //생성자

    IntValue (int v) { this( ); value = v; undef = false; } //생성자

    int intValue ( ) {
        assert !undef : "reference to undefined int value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public void display (int level) {
        super.display(level);
        System.out.print(value);//int값 출력
    }
}

class BoolValue extends Value {
    private boolean value = false;

    BoolValue ( ) { type = Type.BOOL; }

    BoolValue (boolean v) { this( ); value = v; undef = false; }

    boolean boolValue ( ) {
        assert !undef : "reference to undefined bool value";
        return value;
    }

    int intValue ( ) {
        assert !undef : "reference to undefined bool value";
        return value ? 1 : 0;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public void display (int level) {
        super.display(level);
        System.out.print(value); //Boolean값 출력
    }
}

class CharValue extends Value {
    private char value = ' ';

    CharValue ( ) { type = Type.CHAR; }

    CharValue (char v) { this( ); value = v; undef = false; }

    char charValue ( ) {
        assert !undef : "reference to undefined char value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public void display (int level) {
        super.display(level);
        System.out.print(value); //Char값 출력
    }
}

class FloatValue extends Value {
    private float value = 0;

    FloatValue ( ) { type = Type.FLOAT; }

    FloatValue (float v) { this( ); value = v; undef = false; }

    float floatValue ( ) {
        assert !undef : "reference to undefined float value";
        return value;
    }

    public String toString( ) {
        if (undef)  return "undef";
        return "" + value;
    }

    public void display (int level) {
        super.display(level);
        System.out.print(value); //Float값 출력
    }
}

class Binary extends Expression {
// Binary = Operator op; Expression term1, term2
    Operator op;
    Expression term1, term2; //두개 항(term) 계산

    Binary (Operator o, Expression l, Expression r) {
        op = o; term1 = l; term2 = r;
    } // binary생성자

    public void display (int level) {
       super.display(level);
       op.display(level+1);
       term1.display(level+1);
       term2.display(level+1);
    }
} //binary 는 expression을 상속받는다. 두개의 연산을 수행한다.

class Unary extends Expression {
    // Unary = Operator op; Expression term
    Operator op;
    Expression term;

    Unary (Operator o, Expression e) {
        op = o.val.equals("-") ? new Operator("neg"): o;
        term = e;
    } // unary

    public void display (int level) {
       super.display(level);
       op.display(level+1);
       term.display(level+1);
    }
} // Unary 클래스는 expression을 상속받는다. 앞에 -,! 이 붙는 연산 unary

class Operator {
    // Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
    // BooleanOp = && | ||
    final static String AND = "&&";
    final static String OR = "||";
    // RelationalOp = < | <= | == | != | >= | >
    final static String LT = "<";
    final static String LE = "<=";
    final static String EQ = "==";
    final static String NE = "!=";
    final static String GT = ">";
    final static String GE = ">=";
    // ArithmeticOp = + | - | * | /
    final static String PLUS = "+";
    final static String MINUS = "-";
    final static String TIMES = "*";
    final static String DIV = "/";
    // UnaryOp = !
    final static String NOT = "!";
    final static String NEG = "-";
    // CastOp = int | float | char
    final static String INT = "int";
    final static String FLOAT = "float";
    final static String CHAR = "char";
    // Typed Operators
    // RelationalOp = < | <= | == | != | >= | >
    final static String INT_LT = "INT<";
    final static String INT_LE = "INT<=";
    final static String INT_EQ = "INT==";
    final static String INT_NE = "INT!=";
    final static String INT_GT = "INT>";
    final static String INT_GE = "INT>=";
    // ArithmeticOp = + | - | * | /
    final static String INT_PLUS = "INT+";
    final static String INT_MINUS = "INT-";
    final static String INT_TIMES = "INT*";
    final static String INT_DIV = "INT/";
    // UnaryOp = !
    final static String INT_NEG = "-";
    // RelationalOp = < | <= | == | != | >= | >
    final static String FLOAT_LT = "FLOAT<";
    final static String FLOAT_LE = "FLOAT<=";
    final static String FLOAT_EQ = "FLOAT==";
    final static String FLOAT_NE = "FLOAT!=";
    final static String FLOAT_GT = "FLOAT>";
    final static String FLOAT_GE = "FLOAT>=";
    // ArithmeticOp = + | - | * | /
    final static String FLOAT_PLUS = "FLOAT+";
    final static String FLOAT_MINUS = "FLOAT-";
    final static String FLOAT_TIMES = "FLOAT*";
    final static String FLOAT_DIV = "FLOAT/";
    // UnaryOp = !
    final static String FLOAT_NEG = "-";
    // RelationalOp = < | <= | == | != | >= | >
    final static String CHAR_LT = "CHAR<";
    final static String CHAR_LE = "CHAR<=";
    final static String CHAR_EQ = "CHAR==";
    final static String CHAR_NE = "CHAR!=";
    final static String CHAR_GT = "CHAR>";
    final static String CHAR_GE = "CHAR>=";
    // RelationalOp = < | <= | == | != | >= | >
    final static String BOOL_LT = "BOOL<";
    final static String BOOL_LE = "BOOL<=";
    final static String BOOL_EQ = "BOOL==";
    final static String BOOL_NE = "BOOL!=";
    final static String BOOL_GT = "BOOL>";
    final static String BOOL_GE = "BOOL>=";
    // Type specific cast
    final static String I2F = "I2F";
    final static String F2I = "F2I";
    final static String C2I = "C2I";
    final static String I2C = "I2C";

    String val;

    Operator (String s) { val = s; }

    public String toString( ) { return val; }
    public boolean equals(Object obj) { return val.equals(obj); }

    boolean BooleanOp ( ) { return val.equals(AND) || val.equals(OR); }
    boolean RelationalOp ( ) {
        return val.equals(LT) || val.equals(LE) || val.equals(EQ)
            || val.equals(NE) || val.equals(GT) || val.equals(GE);
    }
    boolean ArithmeticOp ( ) {
        return val.equals(PLUS) || val.equals(MINUS)
            || val.equals(TIMES) || val.equals(DIV);
    }
    boolean NotOp ( ) { return val.equals(NOT) ; }
    boolean NegateOp ( ) { return val.equals(NEG) ; }
    boolean intOp ( ) { return val.equals(INT); }
    boolean floatOp ( ) { return val.equals(FLOAT); }
    boolean charOp ( ) { return val.equals(CHAR); }

    final static String intMap[ ] [ ] = {
        {PLUS, INT_PLUS}, {MINUS, INT_MINUS},
        {TIMES, INT_TIMES}, {DIV, INT_DIV},
        {EQ, INT_EQ}, {NE, INT_NE}, {LT, INT_LT},
        {LE, INT_LE}, {GT, INT_GT}, {GE, INT_GE},
        {NEG, INT_NEG}, {FLOAT, I2F}, {CHAR, I2C}
    };

    final static String floatMap[ ] [ ] = {
        {PLUS, FLOAT_PLUS}, {MINUS, FLOAT_MINUS},
        {TIMES, FLOAT_TIMES}, {DIV, FLOAT_DIV},
        {EQ, FLOAT_EQ}, {NE, FLOAT_NE}, {LT, FLOAT_LT},
        {LE, FLOAT_LE}, {GT, FLOAT_GT}, {GE, FLOAT_GE},
        {NEG, FLOAT_NEG}, {INT, F2I}
    };

    final static String charMap[ ] [ ] = {
        {EQ, CHAR_EQ}, {NE, CHAR_NE}, {LT, CHAR_LT},
        {LE, CHAR_LE}, {GT, CHAR_GT}, {GE, CHAR_GE},
        {INT, C2I}
    };

    final static String boolMap[ ] [ ] = {
        {EQ, BOOL_EQ}, {NE, BOOL_NE}, {LT, BOOL_LT},
        {LE, BOOL_LE}, {GT, BOOL_GT}, {GE, BOOL_GE},
    };

    final static private Operator map (String[][] tmap, String op) {
        for (int i = 0; i < tmap.length; i++)
            if (tmap[i][0].equals(op))
                return new Operator(tmap[i][1]);
        assert false : "should never reach here";
        return null;
    }

    final static public Operator intMap (String op) {
        return map (intMap, op);
    }

    final static public Operator floatMap (String op) {
        return map (floatMap, op);
    }

    final static public Operator charMap (String op) {
        return map (charMap, op);
    }

    final static public Operator boolMap (String op) {
        return map (boolMap, op);
    }

    public void display (int level) {
         Indenter indent = new Indenter(level);
         indent.display(getClass().toString().substring(6) + ": " + val);
    }

}
