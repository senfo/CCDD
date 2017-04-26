/**
 * CFS Command & Data Dictionary variable paths & names dialog. Copyright 2017
 * United States Government as represented by the Administrator of the National
 * Aeronautics and Space Administration. No copyright is claimed in the United
 * States under Title 17, U.S. Code. All Other Rights Reserved.
 */
package CCDD;

import static CCDD.CcddConstants.CANCEL_BUTTON;
import static CCDD.CcddConstants.CELL_FONT;
import static CCDD.CcddConstants.CLOSE_ICON;
import static CCDD.CcddConstants.LABEL_FONT_BOLD;
import static CCDD.CcddConstants.LABEL_FONT_PLAIN;
import static CCDD.CcddConstants.LABEL_HORIZONTAL_SPACING;
import static CCDD.CcddConstants.LABEL_TEXT_COLOR;
import static CCDD.CcddConstants.LABEL_VERTICAL_SPACING;
import static CCDD.CcddConstants.PRINT_ICON;
import static CCDD.CcddConstants.RENAME_ICON;
import static CCDD.CcddConstants.TABLE_BACK_COLOR;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import CCDD.CcddBackgroundCommand.BackgroundCommand;
import CCDD.CcddConstants.DefaultPrimitiveTypeInfo;
import CCDD.CcddConstants.DialogOption;
import CCDD.CcddConstants.TableSelectionMode;

/******************************************************************************
 * CFS Command & Data Dictionary variable paths & names dialog class
 *****************************************************************************/
@SuppressWarnings("serial")
public class CcddVariablesDialog extends CcddDialogHandler
{
    // Class references
    private final CcddMain ccddMain;
    private CcddJTableHandler variableTable;
    private final CcddVariableConversionHandler variableHandler;

    // Components referenced from multiple methods
    private JTextField varPathSepFld;
    private JTextField typeNameSepFld;
    private JCheckBox hideDataTypeCb;

    // Total number of variables
    private int numVariables;

    // Variables table data
    private Object[][] tableData;

    /**************************************************************************
     * Variable paths & names dialog class constructor
     * 
     * @param ccddMain
     *            main class reference
     *************************************************************************/
    CcddVariablesDialog(CcddMain ccddMain)
    {
        this.ccddMain = ccddMain;

        // Create the variable handler
        variableHandler = new CcddVariableConversionHandler(ccddMain);

        // Create the variable paths & names dialog
        initialize();
    }

    /**************************************************************************
     * Create the variable paths & names dialog. This is executed in a separate
     * thread since it can take a noticeable amount time to complete, and by
     * using a separate thread the GUI is allowed to continue to update. The
     * GUI menu commands, however, are disabled until the telemetry scheduler
     * initialization completes execution
     *************************************************************************/
    private void initialize()
    {
        // Build the variable paths & names dialog in the background
        CcddBackgroundCommand.executeInBackground(ccddMain, new BackgroundCommand()
        {
            // Create a panel to hold the components of the dialog
            JPanel dialogPnl = new JPanel(new GridBagLayout());
            JPanel buttonPnl = new JPanel();

            /******************************************************************
             * Build the variable paths & names dialog
             *****************************************************************/
            @Override
            protected void execute()
            {
                // Store the total number of variables
                numVariables = variableHandler.getAllVariableNameList().size();

                tableData = new Object[numVariables][2];

                // Step through each row in the variables table
                for (int row = 0; row < numVariables; row++)
                {
                    // Get the variable path and name,removing the bit length
                    // (if present)
                    tableData[row][0] = variableHandler.getAllVariableNameList().get(row).toString();
                }

                // Create a border for the dialog components
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                                                                                                   Color.LIGHT_GRAY,
                                                                                                   Color.GRAY),
                                                                   BorderFactory.createEmptyBorder(2, 2, 2, 2));

                // Set the initial layout manager characteristics
                GridBagConstraints gbc = new GridBagConstraints(0,
                                                                0,
                                                                1,
                                                                1,
                                                                1.0,
                                                                0.0,
                                                                GridBagConstraints.LINE_START,
                                                                GridBagConstraints.BOTH,
                                                                new Insets(LABEL_VERTICAL_SPACING / 2,
                                                                           LABEL_HORIZONTAL_SPACING,
                                                                           LABEL_VERTICAL_SPACING / 2,
                                                                           LABEL_HORIZONTAL_SPACING),
                                                                0,
                                                                0);

                dialogPnl.setBorder(BorderFactory.createEmptyBorder());

                // Create the variable path separator label and input field,
                // and add them to the dialog panel
                JLabel varPathSepLbl = new JLabel("Enter variable path separator character(s)");
                varPathSepLbl.setFont(LABEL_FONT_BOLD);
                dialogPnl.add(varPathSepLbl, gbc);
                varPathSepFld = new JTextField("_", 10);
                varPathSepFld.setFont(LABEL_FONT_PLAIN);
                varPathSepFld.setEditable(true);
                varPathSepFld.setForeground(Color.BLACK);
                varPathSepFld.setBackground(Color.WHITE);
                varPathSepFld.setBorder(border);
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.insets.left = LABEL_HORIZONTAL_SPACING * 2;
                gbc.gridy++;
                dialogPnl.add(varPathSepFld, gbc);

                // Create the data type/variable name separator label and input
                // field, and add them to the dialog panel
                final JLabel typeNameSepLbl = new JLabel("Enter data type/variable name separator character(s)");
                typeNameSepLbl.setFont(LABEL_FONT_BOLD);
                gbc.insets.left = LABEL_HORIZONTAL_SPACING;
                gbc.gridy++;
                dialogPnl.add(typeNameSepLbl, gbc);
                typeNameSepFld = new JTextField("_", 10);
                typeNameSepFld.setFont(LABEL_FONT_PLAIN);
                typeNameSepFld.setEditable(true);
                typeNameSepFld.setForeground(Color.BLACK);
                typeNameSepFld.setBackground(Color.WHITE);
                typeNameSepFld.setBorder(border);
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.insets.left = LABEL_HORIZONTAL_SPACING * 2;
                gbc.gridy++;
                dialogPnl.add(typeNameSepFld, gbc);

                // Create a check box for hiding data types
                hideDataTypeCb = new JCheckBox("Hide data types");
                hideDataTypeCb.setFont(LABEL_FONT_BOLD);
                hideDataTypeCb.setBorder(BorderFactory.createEmptyBorder());
                gbc.insets.bottom = LABEL_VERTICAL_SPACING / 2;
                gbc.gridy++;
                dialogPnl.add(hideDataTypeCb, gbc);

                // Add a listener for the hide data type check box
                hideDataTypeCb.addActionListener(new ActionListener()
                {
                    /**********************************************************
                     * Handle a change in the hide data type check box status
                     *********************************************************/
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        // Enable/disable the data type/variable name separator
                        // input label and field
                        typeNameSepLbl.setEnabled(!((JCheckBox) ae.getSource()).isSelected());
                        typeNameSepFld.setEnabled(!((JCheckBox) ae.getSource()).isSelected());
                    }
                });

                // Create the variables dialog labels and fields
                JLabel variablesLbl = new JLabel("Variables");
                variablesLbl.setFont(LABEL_FONT_BOLD);
                variablesLbl.setForeground(LABEL_TEXT_COLOR);
                gbc.insets.top = LABEL_VERTICAL_SPACING;
                gbc.insets.left = LABEL_HORIZONTAL_SPACING;
                gbc.insets.bottom = 0;
                gbc.gridy++;
                dialogPnl.add(variablesLbl, gbc);

                // Define the variable paths & names JTable
                variableTable = new CcddJTableHandler(DefaultPrimitiveTypeInfo.values().length)
                {
                    /**********************************************************
                     * Allow multiple line display in all columns
                     *********************************************************/
                    @Override
                    protected boolean isColumnMultiLine(int column)
                    {
                        return true;
                    }

                    /**********************************************************
                     * Load the structure table variables paths & names into
                     * the table and format the table cells
                     *********************************************************/
                    @Override
                    protected void loadAndFormatData()
                    {
                        // Step through each row in the variables table
                        for (int row = 0; row < numVariables; row++)
                        {
                            // Display the variable path and name with or
                            // without the data types (based on the check box),
                            // replacing the commas with the separator
                            // character(s), and by changing the array member
                            // left brackets to underscores and removing the
                            // array member right brackets
                            tableData[row][1] = variableHandler.getFullVariableName(tableData[row][0].toString(),
                                                                                    varPathSepFld.getText(),
                                                                                    hideDataTypeCb.isSelected(),
                                                                                    typeNameSepFld.getText());
                        }

                        // Place the data into the table model along with the
                        // column names, set up the editors and renderers for
                        // the table cells, set up the table grid lines, and
                        // calculate the minimum width required to display the
                        // table information
                        setUpdatableCharacteristics(tableData,
                                                    new String[] {"Application Format",
                                                                  "User Format"},
                                                    null,
                                                    null,
                                                    null,
                                                    new String[] {"Variable name with structure path as defined within the application",
                                                                  "Variable name with structure path as specified by user input"},
                                                    false,
                                                    true,
                                                    true,
                                                    true);
                    }
                };

                // Place the table into a scroll pane
                JScrollPane scrollPane = new JScrollPane(variableTable);

                // Set common table parameters and characteristics
                variableTable.setFixedCharacteristics(scrollPane,
                                                      false,
                                                      ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                                                      TableSelectionMode.SELECT_BY_CELL,
                                                      true,
                                                      TABLE_BACK_COLOR,
                                                      true,
                                                      false,
                                                      CELL_FONT,
                                                      true);

                // Define the panel to contain the table
                JPanel variablesTblPnl = new JPanel();
                variablesTblPnl.setLayout(new BoxLayout(variablesTblPnl, BoxLayout.X_AXIS));
                variablesTblPnl.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                variablesTblPnl.add(scrollPane);

                // Add the table to the dialog
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weighty = 1.0;
                gbc.gridx = 0;
                gbc.gridy++;
                dialogPnl.add(variablesTblPnl, gbc);

                // Show variables button
                JButton btnShow = CcddButtonPanelHandler.createButton("Show",
                                                                      RENAME_ICON,
                                                                      KeyEvent.VK_O,
                                                                      "Show the project variables");

                // Add a listener for the Show button
                btnShow.addActionListener(new ActionListener()
                {
                    /**********************************************************
                     * Convert the variables and display the results
                     *********************************************************/
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        // Remove any excess white space
                        varPathSepFld.setText(varPathSepFld.getText().trim());
                        typeNameSepFld.setText(typeNameSepFld.getText().trim());

                        // Check if a separator field contains a character that
                        // cannot be used
                        if (varPathSepFld.getText().matches(".*[\\[\\]].*")
                            || (!hideDataTypeCb.isSelected()
                            && typeNameSepFld.getText().matches(".*[\\[\\]].*")))
                        {
                            // Inform the user that the input value is invalid
                            new CcddDialogHandler().showMessageDialog(CcddVariablesDialog.this,
                                                                      "<html><b>Invalid character(s) in separator field(s)",
                                                                      "Invalid Input",
                                                                      JOptionPane.WARNING_MESSAGE,
                                                                      DialogOption.OK_OPTION);
                        }
                        // The separator fields are valid
                        else
                        {
                            // Convert the variables and display the results in
                            // the table
                            variableTable.loadAndFormatData();
                        }
                    }
                });

                // Print inconsistencies button
                JButton btnPrint = CcddButtonPanelHandler.createButton("Print",
                                                                       PRINT_ICON,
                                                                       KeyEvent.VK_P,
                                                                       "Print the variables list");

                // Add a listener for the Print button
                btnPrint.addActionListener(new ActionListener()
                {
                    /**********************************************************
                     * Print the variables list
                     *********************************************************/
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        variableTable.printTable("Project '"
                                                 + ccddMain.getDbControlHandler().getDatabase()
                                                 + "' Variables",
                                                 null,
                                                 CcddVariablesDialog.this,
                                                 PageFormat.LANDSCAPE);
                    }
                });

                // Close variables dialog button
                JButton btnCancel = CcddButtonPanelHandler.createButton("Close",
                                                                        CLOSE_ICON,
                                                                        KeyEvent.VK_C,
                                                                        "Close the variables dialog");

                // Add a listener for the Close button
                btnCancel.addActionListener(new ActionListener()
                {
                    /**********************************************************
                     * Close the variables dialog
                     *********************************************************/
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        closeDialog(CANCEL_BUTTON);
                    }
                });

                // Create a panel for the dialog buttons and add the buttons to
                // the panel
                buttonPnl.setBorder(BorderFactory.createEmptyBorder());
                buttonPnl.add(btnShow);
                buttonPnl.add(btnPrint);
                buttonPnl.add(btnCancel);
            }

            /******************************************************************
             * Variable paths & names dialog creation complete
             *****************************************************************/
            @Override
            protected void complete()
            {
                // Display the variable name dialog
                showOptionsDialog(ccddMain.getMainFrame(),
                                  dialogPnl,
                                  buttonPnl,
                                  "Variable Paths & Names (" + numVariables + " total)",
                                  true);
            }
        });
    }
}
