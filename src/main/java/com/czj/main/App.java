package com.czj.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

/**
 * Hello world!
 *
 */

public class App {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String pyscriptPath = "C:\\Users\\user\\Desktop\\getLabels.py";

		String labelPath = "C:\\Users\\user\\Desktop\\label2id.pkl";

		List<String> lableList = getlabelsfrompkl(labelPath, pyscriptPath);

		int sequence_lenght = 16;

		Map<String, Integer> vocaMap = readLineListWithVocab("C:\\Users\\user\\Desktop\\vocab.txt");

		String intputString = "希沃金牌讲师团发布的课程";

		int length = intputString.length();

		int[][] inputids = new int[1][sequence_lenght];

		inputids[0][0] = vocaMap.get("[CLS]");

		List<String> tokens = new ArrayList<String>();

		for (int i = 0; i < intputString.length(); i++) {

			inputids[0][i + 1] = vocaMap.get(String.valueOf(intputString.charAt(i)));

			tokens.add(String.valueOf(intputString.charAt(i)));

		}

		inputids[0][length + 1] = vocaMap.get("[SEP]");

		int[][] inputmasks = new int[1][sequence_lenght];

		for (int i = 0; i <= intputString.length() + 1; i++) {

			inputmasks[0][i] = 1;
		}

//		List<String> lableList = new ArrayList<String>(
//				Arrays.asList("I-LOC", "B-PER", "B-LOC", "O", "I-ORG", "B-ORG", "[SEP]", "X", "[CLS]", "I-PER"));
		Map<Integer, String> labelMap = new HashMap<Integer, String>();
		for (int i = 0; i < lableList.size(); i++) {
			labelMap.put(i + 1, lableList.get(i));
		}
//
////		ArrayList<String> lables = new ArrayList<String>();

		try (Graph graph = new Graph()) {
			// 导入图
			byte[] graphBytes = IOUtils.toByteArray(new FileInputStream(
					"C:\\\\Users\\\\user\\\\Documents\\\\GitHub\\\\Semantic-search\\\\target\\\\classes\\\\Bert_Ner\\\\ner_model.pb"));

			graph.importGraphDef(graphBytes);

			// 根据图建立Session
			try (Session session = new Session(graph)) {

				long startTime = System.currentTimeMillis();
				Tensor outpuTensor = session.runner().feed("input_ids", Tensor.create(inputids))
						.feed("input_mask", Tensor.create(inputmasks)).fetch("pred_ids").run().get(0);
				int[][] outArr = new int[1][sequence_lenght];

				outpuTensor.copyTo(outArr);
				long bertinferenceTime = System.currentTimeMillis();
				System.out.println("bert运行时间： " + (bertinferenceTime - startTime) + "ms");
				for (int i = 0; i < outArr[0].length; i++) {
					System.out.println(outArr[0][i]);

				}
				List<String> result = new ArrayList<String>();

				for (int i = 0; i < outArr[0].length; i++) {
					if (outArr[0][i] == 0) {
						System.out.println(i);
						break;
					}
					String currentlabel = labelMap.get(outArr[0][i]);

					if (currentlabel == "[CLS]" || currentlabel == "[SEP]") {
						continue;
					}

					result.add(currentlabel);
				}

				for (int i = 0; i < result.size(); i++) {
					System.out.println(result.get(i));
				}

				List<List<Entity>> resList = result_to_json(tokens, result);
				for (List<Entity> list : resList) {
					System.out.println(list.toString());
				}
				long endTime = System.currentTimeMillis();
				System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
			}
		}

	}

	private static List<String> getlabelsfrompkl(String labelPath, String pyscriptPath) {
		List<String> lableList = new ArrayList<String>();

		try {
			String[] pargs = new String[] { "python", pyscriptPath,

					String.valueOf(labelPath) };

			Process proc = Runtime.getRuntime().exec(pargs);// 执行py文件

			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {

				for (int i = 0; i < line.split("_").length; i++) {
					lableList.add(line.split("_")[i]);
				}

			}
			in.close();
			proc.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return lableList;
	}

	private static List<List<Entity>> result_to_json(List<String> tokens, List<String> tags) {
		List<List<Entity>> resuList = new ArrayList<List<Entity>>();
		List<Entity> locs = new ArrayList<Entity>();
		List<Entity> pers = new ArrayList<Entity>();
		List<Entity> orgs = new ArrayList<Entity>();
		List<Entity> others = new ArrayList<Entity>();

		String entity_name = "";
		int entity_start = 0;
		int idx = 0;
		String last_tag = "";

		for (int i = 0; i < tags.size(); i++) {

			String token = tokens.get(i);
			String tag = tags.get(i);

			if (tag.charAt(0) == 'S') {

//        		self.append(token, idx, idx+1, tag[2:]);

				Entity entity = new Entity(token, idx, idx + 1, last_tag.substring(2));

				if (entity.getType().equals("LOC")) {
					locs.add(entity);
				} else if (entity.getType().equals("PER")) {
					pers.add(entity);
				} else if (entity.getType().equals("ORG")) {
					orgs.add(entity);
				} else {
					others.add(entity);
				}

			} else if (tag.charAt(0) == 'B') {

				if (entity_name != "") {

					Entity entity = new Entity(entity_name, entity_start, idx, last_tag.substring(2));

					if (entity.getType().equals("LOC")) {
						locs.add(entity);
					} else if (entity.getType().equals("PER")) {
						pers.add(entity);
					} else if (entity.getType().equals("ORG")) {
						orgs.add(entity);
					} else {
						others.add(entity);
					}
					entity_name = "";

				}

				entity_name += token;

				entity_start = idx;

			} else if (tag.charAt(0) == 'I') {

				entity_name += token;

			} else if (tag.charAt(0) == 'O') {

				if (entity_name != "") {

					Entity entity = new Entity(entity_name, entity_start, idx, last_tag.substring(2));

					if (entity.getType().equals("LOC")) {
						locs.add(entity);
					} else if (entity.getType().equals("PER")) {
						pers.add(entity);
					} else if (entity.getType().equals("ORG")) {
						orgs.add(entity);
					} else {
						others.add(entity);
					}

					entity_name = "";
				}
			} else {
				entity_name = "";
				entity_start = idx;
			}
			idx += 1;
			last_tag = tag;
		}

		if (entity_name != "") {

//			System.out.println(entity_name);

			Entity entity = new Entity(entity_name, entity_start, idx, last_tag.substring(2));
//			System.out.println(entity.toString());
			if (entity.getType().equals("LOC")) {
				locs.add(entity);
			} else if (entity.getType().equals("PER")) {
				pers.add(entity);
			} else if (entity.getType().equals("ORG")) {
				orgs.add(entity);
			} else {
				others.add(entity);
			}
		}
		resuList.add(pers);
		resuList.add(orgs);
		resuList.add(locs);
		return resuList;
	}

	private static Map<String, Integer> readLineListWithVocab(String path) {

		Map<String, Integer> map = new HashMap<>();

		String line = null;

		int index = 0;
		try {
			BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));

			while ((line = bw.readLine()) != null) {
				map.put(line, index);
				index++;
			}
			bw.close();
		} catch (Exception e) {

		}

		return map;
	}

	private static Tensor<Integer> fromStringToTensor(String strInputIds, int length) {
		// TODO Auto-generated method stub
		int[] arr = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(strInputIds).stream()
				.mapToInt(x -> Integer.valueOf(x)).toArray();
		Preconditions.checkArgument(length == arr.length);
		Tensor<Integer> tensor = Tensors.create(new int[][] { arr });
		return tensor;
	}
}
