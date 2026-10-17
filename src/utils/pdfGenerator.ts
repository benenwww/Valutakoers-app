import { jsPDF } from 'jspdf';
import { DepositItem } from '../types';

export function generateDepositReceiptPDF(
  items: DepositItem[],
  total: number,
  storeName: string
) {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4',
  });

  // Color constants
  const primaryColor = [16, 185, 129]; // Emerald (10, 185, 129)
  const darkSlate = [15, 23, 42]; // Slate-900 (15, 23, 42)
  const textDark = [51, 65, 85]; // Slate-700
  const lightBg = [248, 250, 252]; // Slate-50
  const borderSlate = [226, 232, 240]; // Slate-200

  // Header Box
  doc.setFillColor(darkSlate[0], darkSlate[1], darkSlate[2]);
  doc.rect(0, 0, 210, 45, 'F');

  // Sparkle / Brand Accent bar
  doc.setFillColor(primaryColor[0], primaryColor[1], primaryColor[2]);
  doc.rect(0, 45, 210, 3, 'F');

  // Title
  doc.setTextColor(255, 255, 255);
  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(22);
  doc.text('DEPOSIT RETURN ESTIMATE', 20, 24);
  
  doc.setFont('Helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(148, 163, 184); // Slate-400
  doc.text('Pre-sorted items summary and estimated recycle refund', 20, 31);

  // Logo text or emblem on the right side of header
  doc.setTextColor(16, 185, 129); // emerald-500
  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(15);
  doc.text('PFAND RECYCLE', 190, 24, { align: 'right' });

  // Receipt details grid
  doc.setTextColor(47, 55, 70);
  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(10);
  doc.text('Receipt Date:', 20, 60);
  doc.setFont('Helvetica', 'normal');
  doc.text(new Date().toLocaleString(), 50, 60);

  doc.setFont('Helvetica', 'bold');
  doc.text('Target Store:', 20, 67);
  doc.setFont('Helvetica', 'normal');
  doc.text(storeName ? storeName : 'Any Recyclable Vending System', 50, 67);

  // Items count total metadata
  const totalItemsCount = items.reduce((sum, item) => sum + item.count, 0);
  doc.setFont('Helvetica', 'bold');
  doc.text('Total Items:', 20, 74);
  doc.setFont('Helvetica', 'normal');
  doc.text(`${totalItemsCount} sorted items`, 50, 74);

  // Table header
  doc.setFillColor(lightBg[0], lightBg[1], lightBg[2]);
  doc.rect(20, 83, 170, 10, 'F');
  doc.setDrawColor(borderSlate[0], borderSlate[1], borderSlate[2]);
  doc.line(20, 83, 190, 83);
  doc.line(20, 93, 190, 93);

  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(10);
  doc.setTextColor(51, 65, 85);
  doc.text('Item Description', 24, 89);
  doc.text('Refund Rate', 95, 89, { align: 'right' });
  doc.text('Quantity', 135, 89, { align: 'right' });
  doc.text('Refund Total', 186, 89, { align: 'right' });

  // Table rows
  doc.setFont('Helvetica', 'normal');
  let currentY = 100;
  items.forEach((item, index) => {
    // Row background highlight for alternating rows
    if (index % 2 === 1) {
      doc.setFillColor(248, 250, 252); // Slate-50
      doc.rect(20, currentY - 5, 170, 8, 'F');
    }
    
    doc.setTextColor(textDark[0], textDark[1], textDark[2]);
    // Capitalize item names nicely
    doc.text(item.name, 24, currentY);
    
    // Values text alignment
    doc.text(`€ ${item.value.toFixed(2)}`, 95, currentY, { align: 'right' });
    
    // Bold code/mono look for quantities
    if (item.count > 0) {
      doc.setFont('Helvetica', 'bold');
      doc.setTextColor(16, 185, 129); // Highlight non-zero counts
    } else {
      doc.setTextColor(148, 163, 184);
    }
    doc.text(item.count.toString(), 135, currentY, { align: 'right' });
    
    doc.setFont('Helvetica', 'normal');
    doc.setTextColor(textDark[0], textDark[1], textDark[2]);
    const rowSubtotal = item.value * item.count;
    doc.text(`€ ${rowSubtotal.toFixed(2)}`, 186, currentY, { align: 'right' });
    
    currentY += 8;
  });

  // Table bottom border
  doc.setDrawColor(borderSlate[0], borderSlate[1], borderSlate[2]);
  doc.line(20, currentY - 3, 190, currentY - 3);
  currentY += 5;

  // Running total voucher summary box on right
  const boxWidth = 80;
  const boxHeight = 22;
  const boxX = 110;
  const boxY = currentY;

  // Highlight refund summary box
  doc.setFillColor(240, 253, 244); // green-50
  doc.rect(boxX, boxY, boxWidth, boxHeight, 'F');
  doc.setDrawColor(187, 247, 208); // green-200
  doc.rect(boxX, boxY, boxWidth, boxHeight, 'D');

  doc.setTextColor(22, 101, 52); // green-800
  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(10);
  doc.text('ESTIMATED REFUND', boxX + 6, boxY + 7);
  
  doc.setFontSize(18);
  doc.text(`€ ${total.toFixed(2)}`, boxX + boxWidth - 6, boxY + 15, { align: 'right' });

  // Ecological Impact Summary box on left
  const ecoX = 20;
  const ecoY = currentY;
  const ecoWidth = 82;
  const ecoHeight = 22;

  doc.setFillColor(239, 246, 255); // blue-50
  doc.rect(ecoX, ecoY, ecoWidth, ecoHeight, 'F');
  doc.setDrawColor(191, 219, 254); // blue-200
  doc.rect(ecoX, ecoY, ecoWidth, ecoHeight, 'D');

  doc.setTextColor(30, 58, 138); // blue-900
  doc.setFont('Helvetica', 'bold');
  doc.setFontSize(9);
  doc.text('ENVIRONMENTAL CONTRIBUTION', ecoX + 5, ecoY + 6);
  
  // Calculate impacts
  const canCount = items.find(i => i.id === 'cans')?.count || 0;
  const smallPetCount = items.find(i => i.id === 'smallPet')?.count || 0;
  const bigPetCount = items.find(i => i.id === 'bigPet')?.count || 0;
  const glassCount = items.find(i => i.id === 'glass')?.count || 0;

  const totalPetCount = smallPetCount + bigPetCount;
  const plasticSavedGrams = totalPetCount * 22; // avg 22g plastic
  const metalSavedGrams = canCount * 14; // avg 14g aluminum
  const glassSavedGrams = glassCount * 250; // avg 250g glass
  
  // Total CO2 saved: ~120g CO2 per recycled can/bottle average
  const totalOffsetKg = (canCount * 0.09) + (totalPetCount * 0.12) + (glassCount * 0.3);

  doc.setFont('Helvetica', 'normal');
  doc.setFontSize(8);
  doc.setTextColor(30, 41, 59); // slate-800
  doc.text(`• CO2 offset: ~${totalOffsetKg.toFixed(2)} kg prevented`, ecoX + 5, ecoY + 11);
  doc.text(`• Materials saved: ~${(plasticSavedGrams + metalSavedGrams + glassSavedGrams).toFixed(0)}g repurposed`, ecoX + 5, ecoY + 16);

  currentY += 32;

  // Barcode graphics at the bottom of the voucher
  doc.setDrawColor(15, 23, 42); // slate-900
  let bX = 60;
  const bY = currentY;
  
  // Custom generated elegant pseudo-barcode lines
  const barPattern = [1, 2, 4, 1, 3, 1, 2, 2, 1, 4, 2, 1, 3, 1, 1, 2, 4, 1, 2, 2, 1, 3, 2, 1, 1, 4, 1, 2, 3, 1, 2];
  barPattern.forEach((width, index) => {
    doc.setLineWidth(width * 0.4);
    doc.line(bX, bY, bX, bY + 15);
    bX += (width * 0.4) + 0.6;
  });

  // Code number
  const uniqueCode = `PFD-${Date.now().toString().slice(-6)}-${totalItemsCount}`;
  doc.setFontSize(8);
  doc.setTextColor(100, 116, 139); // Slate-500
  doc.text(uniqueCode, 105, bY + 20, { align: 'center' });

  // Disclaimers at the bottom of the page
  currentY += 28;
  doc.setDrawColor(borderSlate[0], borderSlate[1], borderSlate[2]);
  doc.line(20, currentY, 190, currentY);

  doc.setFontSize(8);
  doc.setFont('Helvetica', 'italic');
  doc.setTextColor(148, 163, 184); // Slate-400
  doc.text('This is a sorting estimation voucher. Official deposit refunds depend on local store verification tools.', 105, currentY + 5, { align: 'center' });
  doc.text('Thank you for making our planet a cleaner place to live!', 105, currentY + 9, { align: 'center' });

  // Save the PDF file
  const dateStr = new Date().toISOString().slice(0, 10);
  doc.save(`Pfand_Refund_Receipt_${dateStr}.pdf`);
}
