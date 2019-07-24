package com.czj.main;

import java.util.ArrayList;
import java.util.List;

public class Result {
	private static List<Entity> locs;
	private static List<Entity> pers;
	private static List<Entity> orgs;
	private static List<Entity> others;

	public List<Entity> getLocs() {
		return locs;
	}

	public void setLocs(List<Entity> locs) {
		this.locs = locs;
	}

	public List<Entity> getPers() {
		return pers;
	}

	public void setPers(List<Entity> pers) {
		this.pers = pers;
	}

	public List<Entity> getOrgs() {
		return orgs;
	}

	public void setOrgs(List<Entity> orgs) {
		this.orgs = orgs;
	}

	public List<Entity> getOthers() {
		return others;
	}

	public void setOthers(List<Entity> others) {
		this.others = others;
	}

//	Result() {
//
//	}

//	public static void append(Entity entity) {
//		if (entity.getType() == "LOC") {
//			locs.add(entity);
//		} else if (entity.getType() == "PER") {
//			pers.add(entity);
//		} else if (entity.getType() == "ORG") {
//			orgs.add(entity);
//		} else {
//			others.add(entity);
//		}
//	}

	public static List<Entity> result_to_json(List<String> tokens, List<String> tags) {
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

				if (entity.getType() == "LOC") {
					locs.add(entity);
				} else if (entity.getType() == "PER") {
					pers.add(entity);
				} else if (entity.getType() == "ORG") {
					orgs.add(entity);
				} else {
					others.add(entity);
				}

			} else if (tag.charAt(0) == 'B') {

				if (entity_name != "") {

					Entity entity = new Entity(entity_name, entity_start, idx, last_tag.substring(2));

					if (entity.getType() == "LOC") {
						locs.add(entity);
					} else if (entity.getType() == "PER") {
						pers.add(entity);
					} else if (entity.getType() == "ORG") {
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

					if (entity.getType() == "LOC") {
						locs.add(entity);
					} else if (entity.getType() == "PER") {
						pers.add(entity);
					} else if (entity.getType() == "ORG") {
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
			Entity entity = new Entity(entity_name, entity_start, idx, last_tag.substring(2));
			if (entity.getType() == "LOC") {
				locs.add(entity);
			} else if (entity.getType() == "PER") {
				pers.add(entity);
			} else if (entity.getType() == "ORG") {
				orgs.add(entity);
			} else {
				others.add(entity);
			}
		}
		return pers;
	}

}
