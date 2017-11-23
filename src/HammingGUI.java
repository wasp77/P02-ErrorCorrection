import java.util.Random;


public class HammingGUI extends javax.swing.JFrame {
    private int padding = 0;
    private int icounter = 0;
    private int[] zeros = new int[7];
    
    public String createRandomData(int length) {
        Random rn = new Random();
        String data = "";
        
        for (int i = 0;  i < length; i++) {
            int random = rn.nextInt(2);
            data += Integer.toString(random);
        }
        return data;
    }
    
    public String padData(String data, int chunkSize) {
        while (data.length() % chunkSize != 0) {
            data += "0";
            padding ++;
        }
        return data;
    }
    
    public String encode(String data, int r) {
        String encodedData = "";
        int dataBits = (int) (Math.pow(2,r) - 1 - r);
        int totalBits = (int) (Math.pow(2,r) - 1);
        char[] bits = new char[totalBits + 1];
        int counter = 0;
        int chunk = dataBits;
        
        if (data.length() % dataBits != 0) {
            data = padData(data, dataBits);
        }

        while (chunk <= data.length()) {
            for (int i = 1; i <= totalBits; i++) {
                if ((i & -i) == i) {
                    bits[i] = '_';
                } else {
                    bits[i] = data.charAt(counter++);
                }
            }
            
            for (int x = 1; x <= totalBits; x++) {
                if (bits[x] == '_') {
                    int onesCounter = 0;
                    int skipCounter = 0;
                    boolean skip = false; 
                    int j = x;
                    while (j <= totalBits) {
                        if (skip == false && bits[j] == '1') {
                            onesCounter++;
                        }
                        skipCounter++;
                        if (skipCounter == x) {
                            skipCounter = 0;
                            skip = !skip;
                        }
                        j++;
                    }
                    if (onesCounter == 0 || onesCounter % 2 == 0) {
                        bits[x] = '0';
                    } else {
                        bits[x] = '1';
                    }
                }
                encodedData += bits[x];
            }       
            chunk += dataBits;
        }
        return encodedData;
    }
    
    public String addError(String encoded, float probability, int num) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        int errorNum = 0;
        float chance;
        sb.append(encoded);
        
        for (int i = 0; i < encoded.length(); i++) {
            chance = r.nextFloat();
            if (chance <= probability) {
                if (sb.charAt(i) == '0') {
                    //System.out.println("error introduced at: " + i);
                    sb.setCharAt(i, '1');
                    errorNum++;
                } else {
                    //System.out.println("error introduced at: " + i);
                    sb.setCharAt(i, '0');
                    errorNum++;
                }  
            }
        }
        
        outputArea.append("Number of errors introduced: " + errorNum + "\n");
        return sb.toString();
    }
    
    public String decode(String data, int r) {
        //System.out.println(data);
        String decodedData = "";
        int totalBits = (int) (Math.pow(2,r) - 1);
        char[] bits = new char[totalBits + 1];
        int[] parityIndex = new int[r];
        int errorCounter;
        int errorLocation;
        int counter = 0;
        int parityCounter = 0;
        int chunk = totalBits;
        
        while (chunk <= data.length()) {
            errorCounter = 0;
            errorLocation = 0;
            parityCounter = 0;
            
            for (int x = 1; x <= totalBits; x++) {
                if ((x & -x) == x) {
                    parityIndex[parityCounter++] = x;
                }
                bits[x] = data.charAt(counter++);
            }
            
            for (int i = 0; i < r; i++) {
                char parityBit = bits[parityIndex[i]];
                int skipCounter = 0;
                int onesCounter = 0;
                boolean skip = false;
                int j = parityIndex[i];
                while(j <= totalBits) {
                    if (j != parityIndex[i] && skip == false && bits[j] == '1') {
                        onesCounter++;
                    }
                    skipCounter++;
                    if (skipCounter == parityIndex[i]) {
                        skipCounter = 0;
                        skip = !skip;
                    } 
                    j++;
                }
                if (onesCounter == 0 || onesCounter % 2 == 0) {
                    if (parityBit != '0') {
                        errorCounter++;
                        errorLocation += parityIndex[i];
                    }
                } else {
                    if (parityBit != '1') {
                        errorCounter++;
                        errorLocation += parityIndex[i];
                    }
                }
            }
        
            if (errorCounter > 1) {
                if (bits[errorLocation] == '0') {
                    //System.out.println("Bit flipped");
                    bits[errorLocation] = '1';
                } else {
                    //System.out.println("Bit flipped");
                    bits[errorLocation] = '0';
                }
            }
                
            for (int x = 1; x <= totalBits; x++) {
                if ((x & -x) != x) {
                    decodedData += bits[x];
                } 
            }
            chunk += totalBits;
        }
       
        return decodedData;
    }
    
    public void checkErrors(String data, String decodedData, int r) {
        int length = data.length();
        int numErrors = 0;
        int[] errorLocations = new int[length];
        int counter = 0;
        
        for (int i = 0; i < length; i++) {
            if (data.charAt(i) != decodedData.charAt(i)) {
                numErrors++;
                errorLocations[counter++] = i;
            }
        }
 
        outputArea.append("Number of errors in decoded message: " + numErrors);
//        if (numErrors > 0) {
//            outputArea.append(" at Location(s): ");
//            for (int x = 0; x < numErrors; x++) {
//                outputArea.append(errorLocations[x] + " ");
//            }
//        }
        outputArea.append("\n");
        
        if (numErrors == 0) {
            zeros[r] += 1;
        }
    }
    
    public void informationRate(int r) {
        int totalBits = (int) (Math.pow(2,r) - 1);
        int dataBits = (int) (Math.pow(2,r) - 1 - r);
        float rate = (float)dataBits / totalBits;
        outputArea.append("Information rate: " + rate + "\n");
    }
    
    public void runHamming(String data, float probability) {
        System.out.println("original: " + data);
        String encoded;
        String decoded;
        
        for (int r = 2; r < 7; r++) {
            outputArea.append("For r = " + r + "\n");
            encoded = encode(data, r);
            System.out.print("encoded: " + encoded + "\n");
            encoded = addError(encoded, probability, r);
            decoded = decode(encoded, r);
            decoded = decoded.substring(0, decoded.length() - padding);
            System.out.print("decoded: " + decoded + "\n");
            checkErrors(data, decoded, r);
            informationRate(r);
            padding = 0;
        }
    }
    
    
    
    
    /**
     * Creates new form HammingGUI
     */
    public HammingGUI() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        probabilityTxt = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
        startBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        dataLengthTxt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Probability: ");

        outputArea.setColumns(20);
        outputArea.setRows(5);
        jScrollPane1.setViewportView(outputArea);

        startBtn.setText("GO");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("Data Length (at least 57):");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(probabilityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataLengthTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(probabilityTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(dataLengthTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
        outputArea.setText("");
        try {
            int length = Integer.parseInt(dataLengthTxt.getText());
            float probability = Float.parseFloat(probabilityTxt.getText());
            if (length >= 57 && probability < 1) {
                String data = createRandomData(length);
                runHamming(data, probability);
            } else {
                outputArea.append("Check input! probability must be less than 1. Data length must be at least 57");
            }    
        } catch (NumberFormatException e) {
            outputArea.append("Check input! probability must be less than 1. Data length must be at least 57");
        }
        
        icounter++;
        if (icounter == 50) {
            startBtn.setEnabled(false);
            icounter = 0;
            for (int r = 2; r < 7; r++) {
                outputArea.append("for r = " + r + " mean: " + (float)zeros[r]/50 + "\n");
            }
        }

    }//GEN-LAST:event_startBtnActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HammingGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dataLengthTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JTextField probabilityTxt;
    private javax.swing.JButton startBtn;
    // End of variables declaration//GEN-END:variables
}