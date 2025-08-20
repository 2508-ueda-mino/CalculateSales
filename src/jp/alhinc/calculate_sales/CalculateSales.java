package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_SERIAL_NUMBER_NAME = "売上ファイル名が連番になっていません";
	private static final String SALESAMOUNT_10_DIGITS_EXCEEDED = "合計金額が10桁を超えました";
	private static final String NOT_CLEAR_INVALID_FORMAT = "<該当ファイル名>の支店コードが不正です";
	private static final String FILENAME_INVALID_FORMAT = "<該当ファイル名>のフォーマットが不正です";


	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();



		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();//売上集計課題をfilesに入れた //パス名は変数で記載した
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			if(files[i].getName().matches("^[0-9]{8}\\.rcd$")){  //ドットは文字列として書いた
				rcdFiles.add(files[i]);
			}


			if (args.length != 1) {

				System.out.println(UNKNOWN_ERROR);
				return;
			}
			 //対象がファイルであり、「数字8桁.rcd」なのか判定したい
			if(args[0].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")) {

				System.out.println(UNKNOWN_ERROR);
				return;

			}
		}
		Collections.sort(rcdFiles);//連判チェックする前に売上ファイルを保持しているListをソートする
		for(int i = 0; i < rcdFiles.size() -1; i++) { //rcdFiles内を順番に連番か確認
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));

			if((latter - former) != 1) { //2つのファイル名の数字を⽐較して、差が1ではなかったら、

				System.out.println(FILE_SERIAL_NUMBER_NAME); //「売上ファイル名が連番になっていません」と表示したい

				return;
			}
		}


		BufferedReader br = null;
		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				File file = new File(args[0], rcdFiles.get(i).getName());  //パス名は変数で記載した


				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				String line; //変数名を行「line」とする
				//ファイルの１行を読み込んでる
				List<String> fileContents = new ArrayList<>();
				while((line = br.readLine()) != null) {
					//読み込んだ内容をリストに入れる

					fileContents.add(line);
					if(fileContents.size() != 2) {
						System.out.println(FILENAME_INVALID_FORMAT);
						return;
					}
				}
				////売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//※詳細は後述で説明
				long fileSale = Long.parseLong(fileContents.get(1)); //String型からLong型へ型変換
				if(!fileContents.get(1).matches("[^0-9]")) { //売上⾦額が数字ではなかった場合は

					System.out.println(UNKNOWN_ERROR); //エラーメッセージをコンソールに表示したい
					return;

				}
				Long saleAmount = branchSales.get(fileContents.get(0)) + fileSale;
				if(saleAmount >= 10000000000L){ //売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表⽰します。
					System.out.println(SALESAMOUNT_10_DIGITS_EXCEEDED);
					return;
				}
				branchSales.put(fileContents.get(0), saleAmount);//加算した売上⾦額をMapに追加します。

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			if(!file.exists()) { //支店定義ファイルが存在しない場合、コンソールにエラーメッセージ「支店定義ファイルが存在しません」を表示したい
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}"))){


					System.out.println(FILE_INVALID_FORMAT); //「支店定義ファイルのフォーマットが不正です」と表示したい
					return false;

				}

				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedWriter bw = null;// ※ここに書き込み処理を作成してください。(処理内容3-1)

		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw= new BufferedWriter(fw);

			// keyが取得できれば売上金額がわかる
			for (String key : branchSales.keySet()) {

				branchSales.get(key);	//売上金額を取り出したい		//出力したい部品①
				// 支店名を取り出したい　//出力したい部品②
				branchNames.get(key);
				// 支店コードを取り出したい　//出力したい部品③
				//key;
				//　取り出した３つを⽀店別集計ファイルに書き込みたい
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));

				bw.newLine(); //改行

				if (!branchNames.containsKey(key)) { //支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
					System.out.println(NOT_CLEAR_INVALID_FORMAT); //<該当ファイル名>の支店コードが不正ですと表示させる
					return false;
				}
			}



		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}


		return true;
	}
}

