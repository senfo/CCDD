/**
 * CFS Command & Data Dictionary variable size and conversion handler.
 *
 * Copyright 2017 United States Government as represented by the Administrator of the National
 * Aeronautics and Space Administration. No copyright is claimed in the United States under Title
 * 17, U.S. Code. All Other Rights Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.DEFAULT_INSTANCE_NODE_NAME;
import static CCDD.CcddConstants.SIZEOF_DATATYPE;
import static CCDD.CcddConstants.TABLE_DESCRIPTION_SEPARATOR;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import CCDD.CcddClasses.ToolTipTreeNode;
import CCDD.CcddConstants.DatabaseListCommand;
import CCDD.CcddConstants.InputDataType;
import CCDD.CcddConstants.InternalTable.ValuesColumn;
import CCDD.CcddConstants.SearchResultsQueryColumn;
import CCDD.CcddConstants.SearchType;
import CCDD.CcddConstants.TableTreeType;
import CCDD.CcddTableTypeHandler.TypeDefinition;

/**************************************************************************************************
 * CFS Command & Data Dictionary variable size and conversion handler class
 *************************************************************************************************/
public class CcddVariableSizeAndConversionHandler
{
    // Class references
    private final CcddMain ccddMain;
    private final CcddDbCommandHandler dbCommand;
    private final CcddTableTypeHandler tableTypeHandler;
    private final CcddDataTypeHandler dataTypeHandler;
    private final CcddMacroHandler macroHandler;

    // Variable offset parameters
    private int bitCount;
    private int lastByteSize;
    private String lastDataType;
    private int lastBitLength;

    // Flag that indicates if a sizeof() call references an invalid data type
    private boolean isInvalid;

    // Table tree with table instances only and including primitive variables
    private CcddTableTreeHandler allVariableTree;

    // List containing the paths for every structure and variable, and the offset to the structures
    // and variables relative to their root structures
    private List<String> structureAndVariablePaths;

    // List containing the offset to the structures and variables relative to their root
    // structures, and the overall structure sizes. The index of a value in this list corresponds
    // to the index of the variable path in the structureAndVariablePaths list
    private List<Integer> structureAndVariableOffsets;

    // List that indicates if the corresponding structure and variable path is a variable. False if
    // the path is for a non-root table and its children. The index of a value in this list
    // corresponds to the index of the variable path in the structureAndVariablePaths list
    private List<Boolean> isVariable;

    // Lists containing all of the program-formatted variable paths, and their corresponding name
    // after converting any commas and brackets based on the specified separator characters. The
    // index of a value in this list corresponds to the index of the variable path in the
    // structureAndVariablePaths list
    private List<String> convertedVariableName;

    // List containing the program-formatted variable paths (key). Only variable paths that have
    // user-defined names are included in this list
    private List<String> userDefinedVariablePathKey;

    // List containing the/ user-defined variable path names. The userDefinedVariablePathKey list
    private List<String> userDefinedVariableName;

    /**********************************************************************************************
     * Variable size and conversion handler class constructor
     *
     * @param ccddMain
     *            main class
     *
     * @param dataTypeHandler
     *            reference to a data type handler; null to use the one in the main class
     *
     * @param macroHandler
     *            reference to a macro handler; null to use the one in the main class
     *********************************************************************************************/
    CcddVariableSizeAndConversionHandler(CcddMain ccddMain,
                                         CcddDataTypeHandler dataTypeHandler,
                                         CcddMacroHandler macroHandler)
    {
        this.ccddMain = ccddMain;
        this.dataTypeHandler = dataTypeHandler == null
                                                       ? ccddMain.getDataTypeHandler()
                                                       : dataTypeHandler;
        this.macroHandler = macroHandler == null
                                                 ? ccddMain.getMacroHandler()
                                                 : macroHandler;

        dbCommand = ccddMain.getDbCommandHandler();
        tableTypeHandler = ccddMain.getTableTypeHandler();
    }

    /**********************************************************************************************
     * Variable size handler class constructor
     *
     * @param ccddMain
     *            main class
     *********************************************************************************************/
    CcddVariableSizeAndConversionHandler(CcddMain ccddMain)
    {
        this(ccddMain, null, null);
    }

    /**********************************************************************************************
     * Get the reference to the table tree of instance structure tables, including the primitive
     * variables
     *
     * @return Reference to the table tree of instance structure tables, including the primitive
     *         variables
     *********************************************************************************************/
    protected CcddTableTreeHandler getVariableTree()
    {
        return allVariableTree;
    }

    /**********************************************************************************************
     * Get the list of structure and variable paths for valid variables
     *
     * @return Reference to the list of structure and variable paths for valid variables
     *********************************************************************************************/
    protected List<String> getAllVariableNames()
    {
        List<String> allVariableNames = new ArrayList<String>();

        // Step through each variable path
        for (int index = 0; index < structureAndVariablePaths.size(); index++)
        {
            // Check if the variable path is a valid variable. The structureAndVariablePaths list
            // includes non-root structures and their children; these are not valid variables (they
            // are in the list for size and offset purposes), so are not included in the list
            // returned
            if (isVariable.get(index))
            {
                // Add the variable path to the list
                allVariableNames.add(structureAndVariablePaths.get(index));
            }
        }

        return allVariableNames;
    }

    /**********************************************************************************************
     * Get the structure and variable paths list
     *
     * @return Reference to the structure and variable paths list
     *********************************************************************************************/
    protected List<String> getStructureAndVariablePaths()
    {
        return structureAndVariablePaths;
    }

    /**********************************************************************************************
     * Get the structure and variable offsets list
     *
     * @return Reference to the structure and variable offsets list
     *********************************************************************************************/
    protected List<Integer> getStructureAndVariableOffsets()
    {
        return structureAndVariableOffsets;
    }

    /**********************************************************************************************
     * Get the regular expression for matching a sizeof() call for the specified data type
     *
     * @param dataType
     *            data type
     *
     * @return Regular expression for matching a sizeof() call for the specified data type
     *********************************************************************************************/
    protected static String getSizeofDataTypeMatch(String dataType)
    {
        return "sizeof\\(+?\\s*(" + dataType + ")\\s*\\)";
    }

    /**********************************************************************************************
     * Check if the supplied text contains a sizeof() call
     *
     * @param text
     *            string in which to search for the sizeof() call
     *
     * @return true if the supplied text contains a sizeof() call
     *********************************************************************************************/
    protected static boolean hasSizeof(String text)
    {
        return text.matches(".*?" + SIZEOF_DATATYPE + ".*");
    }

    /**********************************************************************************************
     * Check if the supplied text contains a sizeof() call for the specified data type
     *
     * @param text
     *            string in which to search for the sizeof() call
     *
     * @param dataType
     *            data type
     *
     * @return true if the supplied text contains a sizeof() call
     *********************************************************************************************/
    protected static boolean hasSizeof(String text, String dataType)
    {
        return text.matches(".*?" + getSizeofDataTypeMatch(dataType) + ".*");
    }

    /**********************************************************************************************
     * Replace each instance of sizeof(data type) in the specified string with its numeric value
     *
     * @param expression
     *            text in which to replace any sizeof() calls
     *
     * @param validDataTypes
     *            List containing the valid data types when evaluating sizeof() calls; null if
     *            there are no data type constraints for a sizeof() call
     *
     * @return Input string with each instance of sizeof(data type) replaced by its numeric value
     *********************************************************************************************/
    protected String replaceSizeofWithValue(String expression, List<String> validDataTypes)
    {
        isInvalid = false;

        // Continue to step through the string, replacing each sizeof() instance
        while (expression != null && hasSizeof(expression))
        {
            // Get the data type (primitive or structure) for the sizeof() call
            String dataType = expression.replaceFirst(".*?" + SIZEOF_DATATYPE + ".*", "$1");

            // Check if the data type isn't in the list of valid ones
            if (validDataTypes != null && !validDataTypes.contains(dataType))
            {
                // Set the flag to indicate an invalid data type reference is made in a sizeof()
                // call
                isInvalid = true;
            }

            // Get the size of the data type in bytes and replace the sizeof() call with this value
            expression = expression.replaceFirst(SIZEOF_DATATYPE,
                                                 String.valueOf(getDataTypeSizeInBytes(dataType)));
        }

        return expression;
    }

    /**********************************************************************************************
     * Check if the text string in the previous replaceSizeofWithValue() call contained an invalid
     * data type reference
     *
     * @return true if the text string in the previous replaceSizeofWithValue() call contained an
     *         invalid data type reference
     *********************************************************************************************/
    protected boolean isInvalidReference()
    {
        return isInvalid;
    }

    /**********************************************************************************************
     * Get the size in bytes of the specified primitive or structure data type
     *
     * @param dataType
     *            structure name or primitive data type
     *
     * @return Size in bytes required to store the data type; returns 0 if the data type doesn't
     *         exist
     *********************************************************************************************/
    protected int getDataTypeSizeInBytes(String dataType)
    {
        int sizeInBytes = 0;

        // Check if the data type is a primitive
        if (dataTypeHandler.isPrimitive(dataType))
        {
            sizeInBytes = dataTypeHandler.getSizeInBytes(dataType);
        }
        // The data type isn't a primitive; check for a structure
        else
        {
            // Get the index in the path list for the specified structure or variable. Remove the
            // bit length if provided
            int index = structureAndVariablePaths.indexOf(dataType);

            // Check if the target exists
            if (index != -1)
            {
                // Get the size of the structure
                sizeInBytes = structureAndVariableOffsets.get(index);
            }
        }

        return sizeInBytes;
    }

    /**********************************************************************************************
     * Get the byte offset of the specified variable relative to its root structure. The variable's
     * path, including data type and variable name, is used to verify that the specified target has
     * been located; i.e., not another variable with the same name
     *
     * @param targetVariable
     *            a comma separated string of the root structure and each data type and variable
     *            name of each variable in the current search path. The bit length may be omitted
     *            for bit-wise variables
     *
     * @return The byte offset to the target prototype structure, or variable relative to its root
     *         structure; returns -1 if the prototype structure name or root-variable path
     *         combination is invalid
     *********************************************************************************************/
    protected int getVariableOffset(String targetVariable)
    {
        int offset = -1;

        // Get the index into the variable path list for the specified structure/variable. A
        // variable's bit length is ignored if present
        int index = structureAndVariablePaths.indexOf(macroHandler.getMacroExpansion(targetVariable).replaceFirst(":.+$", ""));

        // Check that the structure/variable exists
        if (index != -1)
        {
            // Check if the target includes a variable
            if (targetVariable.contains(","))
            {
                // Retrieve the variable's offset
                offset = structureAndVariableOffsets.get(index);
            }
            // The target is a prototype/root structure
            else
            {
                // The offset for a prototype/root structure is always 0; the offset list value for
                // a prototype/root structure name is the structure size
                offset = 0;
            }
        }

        return offset;
    }

    /**********************************************************************************************
     * Using a variable tree create three lists: (1) references to every structure and variable
     * (keeping the child structures and variables in the order in which they appear relative to
     * their root structure), (2) offsets for the variables relative to their root structure, or
     * the total structure size in bytes if the path is for a root structure, and (3) flags
     * indicating if the variable is not for a non-root structure or its children. The conversion
     * list is reset, so the next request for a converted variable path triggers generation of the
     * conversion lists
     *********************************************************************************************/
    protected void buildPathAndOffsetLists()
    {
        structureAndVariablePaths = new ArrayList<String>();
        structureAndVariableOffsets = new ArrayList<Integer>();
        isVariable = new ArrayList<Boolean>();
        convertedVariableName = null;

        int lastIndex = 0;
        int structIndex = 0;

        // Initialize the offset, bit count, and the previous variable's size, type, and bit length
        int offset = 0;
        bitCount = 0;
        lastByteSize = 0;
        lastDataType = "";
        lastBitLength = 0;

        // Create a tree containing all of the structures, both prototypes and instances, including
        // primitive variables. This is used for determining bit-packing, variable relative
        // position, variable offsets, and structure sizes. The prototypes (non-roots) are required
        // in order to calculate the offsets, etc. for instances of the prototype
        allVariableTree = new CcddTableTreeHandler(ccddMain,
                                                   TableTreeType.STRUCTURES_WITH_PRIMITIVES,
                                                   ccddMain.getMainFrame());

        // Step through all of the nodes in the variable tree
        for (Enumeration<?> element = allVariableTree.getRootNode().preorderEnumeration(); element.hasMoreElements();)
        {
            // Get the path to this node
            TreeNode[] nodePath = ((ToolTipTreeNode) element.nextElement()).getPath();

            // Check if the path references a structure or variable (instead of the tree's root or
            // header nodes)
            if (nodePath.length > allVariableTree.getHeaderNodeLevel())
            {
                // Get the variable path for this tree node
                String varPath = allVariableTree.getFullVariablePath(nodePath);

                // Check if the variable path has any macros in its variable name(s)
                if (CcddMacroHandler.hasMacro(varPath))
                {
                    // Expand any macros contained in the variable name(s)
                    varPath = macroHandler.getMacroExpansion(varPath);
                }

                // Check if the path contains a data type
                if (varPath.matches(".+,.+\\..+"))
                {
                    // Extract the data type from the variable path
                    String dataType = varPath.substring(varPath.lastIndexOf(",") + 1,
                                                        varPath.lastIndexOf("."));

                    // Check if this references a primitive data type
                    if (dataTypeHandler.isPrimitive(dataType))
                    {
                        String bitLength = "";

                        int bitIndex = varPath.indexOf(":");

                        // Check if this variable has a bit length
                        if (bitIndex != -1)
                        {
                            // Extract the bit length from the variable path
                            bitLength = varPath.substring(bitIndex + 1);

                            // Remove the bit length from the variable path
                            varPath = varPath.substring(0, bitIndex);
                        }

                        // Adjust the offset to account for bit-packing
                        offset = adjustVariableOffset(dataType, bitLength, offset);
                    }
                    // Not a primitive data type (i.e., it's a structure)
                    else
                    {
                        // Add the last variable's byte size to the offset total
                        offset += lastByteSize;

                        // Reinitialize the bit count, and the previous variable's size, type, and
                        // bit length
                        bitCount = 0;
                        lastByteSize = 0;
                        lastDataType = "";
                        lastBitLength = 0;
                    }
                }
                // The path doesn't contain a data type; i.e., it's a prototype structure reference
                else
                {
                    // Check that this isn't the first prototype structure detected. The size is
                    // stored once the end of the structure is reached
                    if (lastIndex != 0)
                    {
                        // Adjust the offset to account for bit-packing
                        offset = adjustVariableOffset(lastDataType, "", offset);

                        // Store the offset as the size for this structure
                        structureAndVariableOffsets.set(structIndex, offset);

                        // Store the index of the prototype structure
                        structIndex = lastIndex;
                    }

                    // Reset the offset since this indicates the start of a new root structure.
                    // Initialize the bit count, and the previous variable's size, type, and bit
                    // length
                    offset = 0;
                    bitCount = 0;
                    lastByteSize = 0;
                    lastDataType = "";
                    lastBitLength = 0;
                }

                // Update the index pointing to the last member of the structure
                lastIndex++;

                // Add the variable path and its offset to the lists
                structureAndVariablePaths.add(varPath);
                structureAndVariableOffsets.add(offset);
                isVariable.add(nodePath[1].toString().equals(DEFAULT_INSTANCE_NODE_NAME));
            }
        }

        // Check that a prototype structure was detected
        if (lastIndex != 0)
        {
            // Adjust the offset to account for bit-packing
            offset = adjustVariableOffset(lastDataType, "", offset);

            // Store the offset as the size for this structure
            structureAndVariableOffsets.set(structIndex, offset);
        }

        // TODO The structureAndVariablePaths list contains children of prototypes that aren't
        // roots. These are needed for size (typedefs) but not for converted paths - how to
        // differentiate?
        // System.out.println("VAR SIZE:"); // TODO
        // int i = 0;
        // for (String s : structureAndVariablePaths)
        // {
        // System.out.println(s + " " + structureAndVariableOffsets.get(i) + " " +
        // isVariable.get(i)); // TODO
        // i++;
        // }
    }

    /**********************************************************************************************
     * Adjust the offset to the current variable based on the last variable's byte size and any bit
     * packing
     *
     * @param dataType
     *            variable's data type
     *
     * @param bitLength
     *            string representing the number of bits used by variable; blank if this is a
     *            non-bit variable
     *
     * @param offset
     *            offset to the previous variable
     *
     * @return The adjusted byte offset to the target variable
     *********************************************************************************************/
    private int adjustVariableOffset(String dataType, String bitLength, int offset)
    {
        // Get the size in bytes based on the variable's data type
        int byteSize = dataTypeHandler.getSizeInBytes(dataType);

        // Get the bit length associated with the variable; use 0 if no bit length is specified
        int bits = bitLength.matches("\\d+")
                                             ? bits = Integer.valueOf(bitLength)
                                             : 0;

        // Update the bit counter using the bit length
        bitCount += bits;

        // Check if the current or previous variable has no bit length specified, the data type
        // changed, or the data type has no room for the requested number of bits
        if (bits == 0
            || lastBitLength == 0
            || !dataType.equals(lastDataType)
            || bitCount > byteSize * 8)
        {
            // Set the bit counter to the current variable's bit length (0 if this is a non-bit
            // variable)
            bitCount = bits;

            // Add the previous parameter's byte size to the offset counter
            offset += lastByteSize;
        }

        // Store the size in bytes, the data type, and bit length for calculating the offset to the
        // next variable
        lastByteSize = byteSize;
        lastDataType = dataType;
        lastBitLength = bits;

        return offset;
    }

    /**********************************************************************************************
     * Retain or remove the data types in the supplied variable path + name based on the input
     * flag, replace the commas in the (which separate each structure variable in the path) with
     * the specified separator character, replace any left brackets with underscores and right
     * brackets with blanks (in case there are any array members in the path), and remove the bit
     * length (if one is present)
     *
     * @param fullName
     *            variable path + name
     *
     * @param varPathSeparator
     *            character(s) to place between variables path members
     *
     * @param excludeDataTypes
     *            true to remove the data types from the variable path + name
     *
     * @param typeNameSeparator
     *            character(s) to place between data types and variable names
     *
     * @return Variable path + name with the data types retained or removed, commas replaced by the
     *         separator character(s), left brackets replaced by underscores, right brackets
     *         removed, and the bit length removed (if present)
     *********************************************************************************************/
    private String convertVariableName(String fullName,
                                       String varPathSeparator,
                                       boolean excludeDataTypes,
                                       String typeNameSeparator)
    {
        // Check if data types are to be excluded
        if (excludeDataTypes)
        {
            // Remove the data types from the variable path + name
            fullName = fullName.replaceAll(",[^\\.]*\\.", ",");
        }
        // Data types are retained
        else
        {
            // Replace the data type/variable name separator with marker characters. These are used
            // to detect and replace the data type and variable name separator below, and prevents
            // collisions between the two separators and their original characters
            fullName = fullName.replaceAll("\\.", "@~~@");
        }

        return fullName.replaceAll("[,]", varPathSeparator)
                       .replaceAll("@~~@", typeNameSeparator)
                       .replaceAll("[\\[]", "_")
                       .replaceAll("\\]", "")
                       .replaceFirst("\\:\\d+$", "");
    }

    /**********************************************************************************************
     * Determine if the supplied variable path is already in use in a structure
     *
     * @param progDefinedPath
     *            variable path + name in the application's internal format
     *
     * @param alternateName
     *            variable path converted using the separator characters, or a user-defined name
     *
     * @return true if the supplied variable path is already in use in a structure
     *********************************************************************************************/
    protected boolean isVariablePathInUse(String progDefinedPath, String alternateName)
    {
        boolean isInUse = false;

        // Locate the alternate name in the list of user-defined variable names
        int index = userDefinedVariableName.indexOf(alternateName);

        // Check if the name was found (i.e., the name matches one manually set by the user)
        if (index != -1)
        {
            // Check if the supplied program-formatted path doesn't correspond with of the
            // user-defined name - if the program-formatted path is the 'key' for the user-defined
            // name then it's the legitimate owner and doesn't constitute a duplicate reference
            if (index != userDefinedVariablePathKey.indexOf(progDefinedPath))
            {
                // Set the flag to indicate the user-defined name is already in use
                isInUse = true;
            }
        }
        // The supplied alternate name is not a user-defined name
        else
        {
            // Locate the alternate name in the list of program-converted variable names
            index = convertedVariableName.indexOf(alternateName);

            // Check if the name was found (i.e., the name matches one manually generated by the
            // program)
            if (index != -1)
            {
                // Check if the supplied program-formatted path doesn't correspond with of the
                // converted name - if the program-formatted path is the 'key' for the converted
                // name then it's the legitimate owner and doesn't constitute a duplicate reference
                if (index != structureAndVariablePaths.indexOf(progDefinedPath))
                {
                    // Set the flag to indicate the converted name is already in use
                    isInUse = true;
                }
            }
        }

        return isInUse;
    }

    /**********************************************************************************************
     * Get a variable's full name which includes the variables in the structure path separated by
     * the specified separator character(s). In case there are any array member variable names in
     * the full name, replace left square brackets with # underscores and remove right square
     * brackets (example: a[0],b[2] becomes a_0separatorb_2)
     *
     * @param fullName
     *            variable path + name in the format rootTable[,structureDataType1.variable1
     *            [,structureDataType2.variable2 [,...]]],primitiveDataType.variable
     *
     * @param varPathSeparator
     *            character(s) to place between variables path members
     *
     * @param excludeDataTypes
     *            true to remove the data types from the variable path + name
     *
     * @param typeNameSeparator
     *            character(s) to place between data types and variable names
     *
     * @return The variable's full path and name with each variable in the path separated by the
     *         specified separator character(s); if a user-defined path exists then it is returned
     *         in place of the auto-generated one. Returns a blank if fullName is null or empty
     *********************************************************************************************/
    protected String getFullVariableName(String fullName,
                                         String varPathSeparator,
                                         boolean excludeDataTypes,
                                         String typeNameSeparator)
    {
        return getFullVariableName(fullName,
                                   varPathSeparator,
                                   excludeDataTypes,
                                   typeNameSeparator,
                                   true);
    }

    /**********************************************************************************************
     * Get a variable's full name which includes the variables in the structure path separated by
     * the specified separator character(s). In case there are any array member variable names in
     * the full name, replace left square brackets with # underscores and remove right square
     * brackets (example: a[0],b[2] becomes a_0separatorb_2)
     *
     * @param fullName
     *            variable path + name in the format rootTable[,structureDataType1.variable1
     *            [,structureDataType2.variable2 [,...]]],primitiveDataType.variable
     *
     * @param varPathSeparator
     *            character(s) to place between variables path members
     *
     * @param excludeDataTypes
     *            true to remove the data types from the variable path + name
     *
     * @param typeNameSeparator
     *            character(s) to place between data types and variable names
     *
     * @param includeCustom
     *            true to substitute the user-defined variable name (if present); false to ignore
     *            the user-defined name and use the auto-generated one based on the conversion
     *            flags
     *
     * @return The variable's full path and name with each variable in the path separated by the
     *         specified separator character(s); returns a blank if fullName is null or empty
     *********************************************************************************************/
    protected String getFullVariableName(String fullName,
                                         String varPathSeparator,
                                         boolean excludeDataTypes,
                                         String typeNameSeparator,
                                         boolean includeCustom)
    {
        String convertedFullName = "";

        // Check if the full variable name is provided
        if (fullName != null && !fullName.isEmpty())
        {
            // Check if the conversion list hasn't been created already
            if (convertedVariableName == null)
            {
                // Create the conversion list. The conversion list is needed since it's possible
                // that duplicate variable path + names can occur if underscores are part of the
                // names. The lists ensure that no duplicate is returned; instead, a unique name is
                // created by appending one or more underscores to the otherwise duplicate name.
                // Variable paths that are explicitly defined by the user are then added to the
                // lists
                createVariableNameList(varPathSeparator, excludeDataTypes, typeNameSeparator);
            }

            // Check if the user-defined variable name should be substituted, if present
            if (includeCustom)
            {
                // Get the index of the variable name from the list of original names
                int index = userDefinedVariablePathKey.indexOf(fullName);

                // Check if the variable name was extracted from the list
                if (index != -1)
                {
                    // Get the converted variable name for this variable. This name has one or more
                    // underscores appended since it would otherwise duplicate another variable's
                    // name
                    convertedFullName = userDefinedVariableName.get(index);
                }
            }

            // Check if the variable path isn't overridden by a user-defined name
            if (convertedFullName.isEmpty())
            {
                // Get the index of the variable path from the list of program-formatted names
                int index = structureAndVariablePaths.indexOf(fullName);

                // Check if the variable name was extracted from the list
                if (index != -1 && convertedVariableName.get(index) != null)
                {
                    // Get the converted variable name for this variable. This name has one or more
                    // underscores appended since it would otherwise duplicate another variable's
                    // name
                    convertedFullName = convertedVariableName.get(index);

                    // Check if data types are to be excluded
                    if (excludeDataTypes)
                    {
                        // Remove the data types from the variable path + name
                        convertedFullName = convertedFullName.replaceAll(",[^\\.]*\\.", ",");
                    }
                }
            }
        }

        return convertedFullName;
    }

    /**********************************************************************************************
     * Create a pair of lists that show a variable's full name before and after converting any
     * commas and brackets to underscores. Check if duplicate variable names result from the
     * conversion; if a duplicate is found append an underscore to the duplicate's name. Once all
     * variable names are processed trim the list to include only those variables that are modified
     * to prevent a duplicate. These lists are used by getFullVariableName() so that it always
     * returns a unique name
     *
     * @param varPathSeparator
     *            character(s) to place between variables path members
     *
     * @param excludeDataTypes
     *            true to remove the data types from the variable path + name
     *
     * @param typeNameSeparator
     *            character(s) to place between data types and variable names
     *********************************************************************************************/
    private void createVariableNameList(String varPathSeparator,
                                        boolean excludeDataTypes,
                                        String typeNameSeparator)
    {
        String varPathColumnsDb = "";
        String varPathColumnsUser = "";
        convertedVariableName = new ArrayList<String>();
        userDefinedVariablePathKey = new ArrayList<String>();
        userDefinedVariableName = new ArrayList<String>();

        // Step through each variable
        for (int index = 0; index < structureAndVariablePaths.size(); index++)
        {
            String fullName = null;

            // Check if the variable path is a valid variable. The structureAndVariablePaths list
            // includes non-root structures and their children; these are not valid variables (they
            // are in the list for size and offset purposes), so are not included in the list
            // returned
            if (isVariable.get(index))
            {
                // Convert the variable path + name using underscores to separate the variables in
                // the path, and retain the data types
                fullName = convertVariableName(structureAndVariablePaths.get(index),
                                               varPathSeparator,
                                               excludeDataTypes,
                                               typeNameSeparator);

                // Compare the converted variable name to those already added to the list
                while (convertedVariableName.contains(fullName))
                {
                    // A matching name already exists; append an underscore to this variable's name
                    fullName += "_";
                }
            }

            // Add the variable name to the converted variable name list
            convertedVariableName.add(fullName);
        }

        // Step through each table type definition
        for (TypeDefinition typeDefn : ccddMain.getTableTypeHandler().getTypeDefinitions())
        {
            // Check if the table type represents a structure
            if (typeDefn.isStructure())
            {
                // Get the index of the column containing the variable path
                int variablePathIndex = typeDefn.getColumnIndexByInputType(InputDataType.VARIABLE_PATH);

                // Check if the variable path column is present
                if (variablePathIndex != -1)
                {
                    // Append the database and user column names to the search criteria
                    varPathColumnsDb += typeDefn.getColumnNamesDatabase()[variablePathIndex] + ",";
                    varPathColumnsUser += ValuesColumn.COLUMN_NAME.getColumnName()
                                          + " = '"
                                          + typeDefn.getColumnNamesUser()[variablePathIndex]
                                          + "' OR ";
                }
            }
        }

        // Check if any variable path column exists
        if (!varPathColumnsDb.isEmpty())
        {
            // Remove the unneeded trailing text
            varPathColumnsDb = CcddUtilities.removeTrailer(varPathColumnsDb, ",");
            varPathColumnsUser = CcddUtilities.removeTrailer(varPathColumnsUser, " OR ");

            // Get the references in the prototype tables that contain user-defined (i.e.,
            // non-blank) variable paths. This accounts for root tables with user-defined paths
            String[] matches = dbCommand.getList(DatabaseListCommand.SEARCH,
                                                 new String[][] {{"_search_text_",
                                                                  ".+"},
                                                                 {"_case_insensitive_",
                                                                  "false"},
                                                                 {"_allow_regex_",
                                                                  "true"},
                                                                 {"_selected_tables_",
                                                                  SearchType.DATA.toString()},
                                                                 {"_columns_",
                                                                  varPathColumnsDb}},
                                                 ccddMain.getMainFrame());

            // Step through each variable path
            for (String match : matches)
            {
                // Split the reference into table name, column name, table type, and context
                String[] tblColDescAndCntxt = match.split(TABLE_DESCRIPTION_SEPARATOR, 4);

                // Create a reference to the search result's database table name and row data to
                // shorten comparisons below
                String[] rowData = CcddUtilities.splitAndRemoveQuotes(tblColDescAndCntxt[SearchResultsQueryColumn.CONTEXT.ordinal()]);

                // Set the viewable table name (with capitalization intact)
                String[] tableNameAndType = tblColDescAndCntxt[SearchResultsQueryColumn.COMMENT.ordinal()].split(",", 2);

                // Get the table's type definition and from that the variable name, data type, and
                // variable path column indices
                TypeDefinition typeDefn = tableTypeHandler.getTypeDefinition(tableNameAndType[1]);
                int variableNameIndex = typeDefn.getColumnIndexByInputType(InputDataType.VARIABLE);
                int dataTypeIndex = typeDefn.getColumnIndexByInputType(InputDataType.PRIM_AND_STRUCT);
                int variablePathIndex = typeDefn.getColumnIndexByInputType(InputDataType.VARIABLE_PATH);

                // Add the variable path to the lists (program- and user-defined)
                userDefinedVariablePathKey.add(tableNameAndType[0]
                                               + ","
                                               + rowData[dataTypeIndex]
                                               + "."
                                               + rowData[variableNameIndex]);
                userDefinedVariableName.add(rowData[variablePathIndex]);
            }

            // Get the references in the custom values table for all user-defined variable paths.
            // This accounts for child tables with user-defined paths
            matches = dbCommand.getList(DatabaseListCommand.VAR_PATH,
                                        new String[][] {{"_match_column_name_",
                                                         varPathColumnsUser}},
                                        ccddMain.getMainFrame());

            // Step through each variable path
            for (String match : matches)
            {
                // Split the reference into table name and variable path
                String[] tableNameAndPath = match.split(TABLE_DESCRIPTION_SEPARATOR, 2);

                // Add the variable path to the lists (program- and user-defined)
                userDefinedVariablePathKey.add(tableNameAndPath[0]);
                userDefinedVariableName.add(tableNameAndPath[1]);
            }
        }
    }
}