package kendzi.robo;


import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

import org.ejml.data.DenseMatrix64F;
import org.ejml.data.SimpleMatrix;

public class RoboUtil {
	public static SimpleMatrix transformToR(SimpleMatrix p,SimpleMatrix p_w) {
		//	%transformacja punktu p do ukladu odniesienia robota o pozycji p_w
		double th = p_w.get(2);
		return R(th).mult(p.minus(p_w));
	}

	static SimpleMatrix R(double th) {
		//		 % transformacja z ukladu odniesienia œwiata do ukladu odniesienia robota
		//		 % k¹t (normalniy) przeciw róchowi wskazowek zegara
		double cosTh = Math.cos(th);
		double sinTh = Math.sin(th);

		return new SimpleMatrix(
				new double [][]  {
						{cosTh, sinTh, 0},
						{-sinTh, cosTh, 0},
						{0, 0, 1}});
	}

	public SimpleMatrix transformToW(SimpleMatrix p,SimpleMatrix p_w) {

		//		% p - punkt ktory transformujemy
		//		% p_w - pozycja robota w swiecie
		//		%transformacja punktu p z ukladu odniesienia robota do ukladu odniesienia swiata z pozycji robota p_w

		double th = p_w.get(2);
		return W(th).mult(p).plus(p_w);
	}

	SimpleMatrix W(double th) {
		//		 % transformacja z ukladu odniesienia œwiata do ukladu odniesienia robota
		double cosTh = Math.cos(th);
		double sinTh = Math.sin(th);

		return new SimpleMatrix(
				new double [][]  {
						{cosTh, -sinTh, 0},
						{sinTh, cosTh, 0},
						{0, 0, 1}});
	}

	public static String matrixToString(SimpleMatrix m) {
		DenseMatrix64F mat = m.getMatrix();
		StringBuilder sb = new StringBuilder();
		// Send all output to the Appendable object sb
		Formatter formatter = new Formatter(sb, Locale.US);

		// Explicit argument indices may be used to re-order output.

		int numChar = 6;
		int precision = 3;
		sb.append("DenseMatrix64F  numRows = " + mat.numRows + " numCols = " + mat.numCols + "\n");

		String format = "%" + numChar + "." + precision + "f ";

		for (int y = 0; y < mat.numRows; y++) {
			for (int x = 0; x < mat.numCols; x++) {

				formatter.format(format, mat.get(y, x));
				// sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();

	}
	public static String matrixToMatlab(SimpleMatrix m) {
		if (m == null) {
			return " [ ]; ";
		}
		if ((m.numRows() == 0) || (m.numCols() ==0)) {
			return " [ ]; ";
		}

		DenseMatrix64F mat = m.getMatrix();
		StringBuilder sb = new StringBuilder();
		// Send all output to the Appendable object sb
		Formatter formatter = new Formatter(sb, Locale.US);

		String space = "        ";

		// Explicit argument indices may be used to re-order output.

		int numChar = 6;
		int precision = 3;
		//sb.append("DenseMatrix64F  numRows = " + mat.numRows + " numCols = " + mat.numCols + "\n");

		String format = "%" + numChar + "." + precision + "f ";
		sb.append(space);
		sb.append("[ ");
		for (int y = 0; y < mat.numRows; y++) {
			for (int x = 0; x < mat.numCols; x++) {
				if (x != 0) {
					sb.append(", ");
				}

				sb.append(mat.get(y, x));

				//				formatter.format(format, mat.get(y, x));
				// sb.append("\n");
			}
			sb.append(" ; \n  ");
			sb.append(space);

		}
		sb.append("];");
		return sb.toString();


	}
	public static String ff(Integer n) {
		return ff(new Double(n));
	}
	public static String ff(Double n) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		int numChar = 6;
		int precision = 3;

		String format = "%" + numChar + "." + precision + "f ";

		formatter.format(format,  n);

		return sb.toString();

	}

	public static String ff2(Number n) {
		String pattern = "0.000E0";
		DecimalFormat myFormatter = new DecimalFormat(pattern);
		String output = myFormatter.format(n);
		return output;
	}
}
