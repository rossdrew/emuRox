package com.rox.emu.processor.mos6502.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.CPU;
import com.rox.emu.processor.mos6502.op.AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  A compiler for a {@link CPU} for taking a textual program and converting it into an executable byte stream, e.g.<br/>
 *  <br/>
 *  <code>
 *      LDA {@value IMMEDIATE_VALUE_PREFIX}52 <br/>
 *      LDX {@value IMMEDIATE_VALUE_PREFIX}10<br/>
 *      STA {@value VALUE_PREFIX}F1{@value X_INDEXED_POSTFIX}<br/>
 *  </code> <br/>
 *  -> <code> [0xA9, 0x52, 0xA2, 0x10, 0x95, 0xF1] </code>
 *
 *  <table>
 *    <tr>
 *        <th>Format</th>
 *        <th>Addressing Mode</th>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value IMMEDIATE_VALUE_PREFIX}V</code></td>
 *      <td>Immediate</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value ACCUMULATOR_PREFIX}VV</code></td>
 *      <td>Accumulator</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}V</code> / <code>{@value VALUE_PREFIX}VV</code></td>
 *      <td>Zero Page</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}V{@value X_INDEXED_POSTFIX}</code> / <code>{@value VALUE_PREFIX}VV{@value X_INDEXED_POSTFIX}</code></td>
 *      <td>Zero Page[X]</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}V{@value Y_INDEXED_POSTFIX}</code> / <code>{@value VALUE_PREFIX}VV{@value Y_INDEXED_POSTFIX}</code></td>
 *      <td>Zero Page[Y]</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}VVV</code> / <code>{@value VALUE_PREFIX}VVVV</code></td>
 *      <td>Absolute</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}VVV{@value X_INDEXED_POSTFIX}</code> / <code>{@value VALUE_PREFIX}VVVV{@value X_INDEXED_POSTFIX}</code></td>
 *      <td>Absolute[X]</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value VALUE_PREFIX}VVV{@value Y_INDEXED_POSTFIX}</code> / <code>{@value VALUE_PREFIX}VVVV{@value Y_INDEXED_POSTFIX}</code></td>
 *      <td>Absolute[Y]</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value INDIRECT_PREFIX}V{@value INDIRECT_X_POSTFIX}</code> / <code>{@value INDIRECT_PREFIX}VV{@value INDIRECT_X_POSTFIX}</code></td>
 *      <td>Indirect, X</td>
 *    </tr>
 *
 *    <tr>
 *      <td><code>{@value INDIRECT_PREFIX}V{@value INDIRECT_Y_POSTFIX}</code> / <code>{@value INDIRECT_PREFIX}VV{@value INDIRECT_Y_POSTFIX}</code></td>
 *      <td>Indirect, Y</td>
 *    </tr>
 *
 *  </Table>
 *
 * @author Ross Drew
 */
public class Compiler {
    /** Regex used to extract an argument prefix */
    public static final Pattern PREFIX_REGEX = Pattern.compile("^[^0-9a-fA-F]{1,4}");
    /** Regex used to extract an argument value */
    public static final Pattern VALUE_REGEX = Pattern.compile("[0-9a-fA-F]+");
    /** Regex used to extract an argument postfix */
    public static final Pattern POSTFIX_REGEX = Pattern.compile("(?<=\\w)(,[XY]|,X\\)|\\),Y)$");
    /** Regex used to extract a program label */
    public static final Pattern LABEL_REGEX = Pattern.compile("\\w+:");

    /** The prefix expected for an accumulator addressed argument */
    public static final String ACCUMULATOR_PREFIX = "#";
    /** The prefix expected for a value argument */
    public static final String VALUE_PREFIX = "$";
    /** The prefix expected for an immediate value argument */
    public static final String IMMEDIATE_VALUE_PREFIX = ACCUMULATOR_PREFIX + VALUE_PREFIX;
    /** The prefix expected for an indirect value.  Value must also be postfixed with INDIRECT_X_POSTFIX( "<code>{@value INDIRECT_X_POSTFIX}</code>" ) or INDIRECT_Y_POSTFIX( "<code>{@value INDIRECT_Y_POSTFIX}</code>" ) */
    public static final String INDIRECT_PREFIX = "(" + VALUE_PREFIX;

    /** The postfix expected for an X indexed argument value */
    public static final String X_INDEXED_POSTFIX = ",X";
    /** The postfix expected for an Y indexed argument value */
    public static final String Y_INDEXED_POSTFIX = ",Y";
    /** The postfix expected for an indexed indirect addressed argument value.  Must also be prefixed with INDIRECT_PREFIX ( "<code>{@value INDIRECT_PREFIX}</code>" ) */
    public static final String INDIRECT_X_POSTFIX = X_INDEXED_POSTFIX + ")";
    /** The postfix expected for an indirect indexed addressed argument value.  Must also be prefixed with INDIRECT_PREFIX ( "<code>{@value INDIRECT_PREFIX}</code>" )  */
    public static final String INDIRECT_Y_POSTFIX = ")" + Y_INDEXED_POSTFIX;
    
    private final String programText;

    /**
     * Create a compiler object around a textual program, ready for compilation
     *
     * @param programText The MOS6502 program as {@link String} ready for compilation
     */
    public Compiler(String programText){
        this.programText = programText;
    }

    /**
     * Extract a compiled {@link Program} from this compiler.
     * This will use the program text contained in this object and attempt to compile it
     *
     * @return A compiled {@link Program} object
     * @throws UnknownOpCodeException if during the course of compilation, we encounter an unknown where we expect an op-code to be
     */
    public Program compileProgram() throws UnknownOpCodeException{
        Program workingProgram = new Program();

        final StringTokenizer tokenizer = new StringTokenizer(programText);
        while (tokenizer.hasMoreTokens()){
            final String opCodeToken = tokenizer.nextToken();

            switch(opCodeToken){
                case "TAX": case "TAY":
                case "TYA": case "TXA": case "TXS": case "TXY": case "TSX":
                case "PHA": case "PLA":
                case "PHP": case "PLP":
                case "INY": case "DEY":
                case "INX": case "DEX":
                case "RTS": case "RTI":
                case "JSR":
                case "BPL": case "BMI": case "BVC": case "BVS": case "BCC": case "BCS": case "BNE": case "BEQ":
                case "SEC": case "CLC":
                case "SEI": case "SED":
                case "CLD": case "CLI": case "CLV":
                case "BRK":
                case "NOP":
                    workingProgram = workingProgram.with(OpCode.from(opCodeToken).getByteValue());
                    break;
                case "ADC": case "SBC":
                case "LDA": case "LDY": case "LDX":
                case "AND": case "ORA": case "EOR":
                case "ASL": case "ROL": case "LSR":
                case "STY": case "STX": case "STA":
                case "CMP": case "CPX": case "CPY":
                case "INC": case "DEC":
                case "BIT":

                case "JMP": //Absolute only

                case "ROR": //Accumulator only
                    final String valueToken = tokenizer.nextToken().trim();
                    final String prefix = extractFirstOccurrence(PREFIX_REGEX, valueToken).trim();
                    final String value = extractFirstOccurrence(VALUE_REGEX, valueToken).trim();
                    final String postfix = extractFirstOccurrence(POSTFIX_REGEX, valueToken).trim();

                    final AddressingMode addressingMode = getAddressingModeFrom(prefix, value, postfix);


                    workingProgram = workingProgram.with(OpCode.from(opCodeToken, addressingMode).getByteValue(),
                                                         Integer.decode("0x" + value));
                    break;
                default:
                    workingProgram = workingProgram.with(parseLabel(opCodeToken));
                    break;
            }
        }

        return workingProgram;
    }

    /**
     * Extract the first occurrence of the given pattern from the given token {@link String}
     *
     * @param pattern The compiled regex {@link Pattern} for the desired search
     * @param token The {@link String} to search by applying the <code>pattern</code>
     * @return The first found occurrence or an empty {@link String}
     */
    public static String extractFirstOccurrence(Pattern pattern, String token){
        final Matcher prefixMatcher = pattern.matcher(token);
        prefixMatcher.find();
        try {
            return prefixMatcher.group(0);
        }catch(IllegalStateException | ArrayIndexOutOfBoundsException e){
            return "";
        }
    }

    private String parseLabel(final String opCodeToken) throws UnknownOpCodeException{
        final String label = extractFirstOccurrence(LABEL_REGEX, opCodeToken);

        if (label.isEmpty())
            throw new UnknownOpCodeException("Unknown op-code (\"" + opCodeToken + "\") while parsing program", opCodeToken);

        return label;
    }

    private AddressingMode getAddressingModeFrom(String prefix, String value, String postfix){
        if (prefix.equalsIgnoreCase(IMMEDIATE_VALUE_PREFIX)) {
            return AddressingMode.IMMEDIATE;
        }else if (prefix.equalsIgnoreCase(ACCUMULATOR_PREFIX)){
            return AddressingMode.ACCUMULATOR;
        }else if (prefix.equalsIgnoreCase(VALUE_PREFIX)){
            return getIndexedAddressingMode(prefix, value, postfix);
        }else if (prefix.equalsIgnoreCase(INDIRECT_PREFIX)){
            return getIndirectIndexMode(prefix, value, postfix);
        }

        throw new UnknownOpCodeException("Invalid or unimplemented argument: '" + prefix + value + postfix + "'", prefix+value);
    }

    private AddressingMode getIndirectIndexMode(String prefix, String value, String postfix){
        if (postfix.equalsIgnoreCase(INDIRECT_X_POSTFIX)){
            return AddressingMode.INDIRECT_X;
        }else if (postfix.equalsIgnoreCase(INDIRECT_Y_POSTFIX)){
            return AddressingMode.INDIRECT_Y;
        }

        throw new UnknownOpCodeException("Invalid or unimplemented argument: '" + prefix + value + postfix + "'", prefix+value);
    }

    private AddressingMode getIndexedAddressingMode(String prefix, String value, String postfix){
        if (value.length() <= 2) {
            return decorateWithIndexingMode(AddressingMode.ZERO_PAGE, postfix);
        }else if (value.length() <= 4){
            return decorateWithIndexingMode(AddressingMode.ABSOLUTE, postfix);
        }

        throw new UnknownOpCodeException("Invalid or unimplemented argument: '" + prefix + value + postfix + "'", prefix+value);
    }

    private AddressingMode decorateWithIndexingMode(AddressingMode addressingMode, String postfix){
        if (postfix.equalsIgnoreCase(X_INDEXED_POSTFIX)){
            return addressingMode.xIndexed();
        }else if (postfix.equalsIgnoreCase(Y_INDEXED_POSTFIX)){
            return addressingMode.yIndexed();
        }

        return addressingMode;
    }
}
