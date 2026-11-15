package com.example.bookstore.category;

import static com.example.bookstore.jooq.Tables.CATEGORY;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.*;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

	private final DSLContext dsl;

	public CategoryRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	/**
	 * WITH RECURSIVE tree(id, parent_id, name, depth, path) AS(
	 *		SELECT id, parent_id, name, 0, cast(name AS text)
	 *		FROM category WHERE parent_id IS NULL
	 * UNION ALL
	 * 		SELECT c.id, c.parent_id, c.name. tree.depth + 1, tree.path || ' > ' || c.name
	 * 		FROM category c JOIN tree ON c.parent_id = tree.id
	 * )
	 * SELECT * FROM tree ORDER BY path;
	 */
	public List<CategoryNode> findTree() {
		// 재귀 부분에서 자기 자신을 참조하기 위한 "이름 기반" 참조
		Table<?> treeRef = table(name("tree"));
		var treeId = field(name("tree", "id"), Long.class);
		var treeDepth = field(name("tree", "depth"), Integer.class);
		var treePath = field(name("tree", "path"), String.class);

		var tree = name("tree")
			.fields("id", "parent_id", "name", "depth", "path").as(
			// 시작점
			select(CATEGORY.ID,
				CATEGORY.PARENT_ID,
				CATEGORY.NAME,
				inline(0),
				CATEGORY.NAME.cast(SQLDataType.CLOB)
			)
				.from(CATEGORY)
				.where(CATEGORY.PARENT_ID.isNull())
				.unionAll(
					select(CATEGORY.ID,
						CATEGORY.PARENT_ID,
						CATEGORY.NAME,
						treeDepth.plus(1),
						treePath.concat(inline(" > "), CATEGORY.NAME)
					)
						.from(CATEGORY)
						.join(treeRef).on(CATEGORY.PARENT_ID.eq(treeId))
				)
			);

		return dsl.withRecursive(tree)
			.select(
				tree.field("id", Long.class),
				tree.field("parent_id", Long.class),
				tree.field("name", String.class),
				tree.field("depth", Integer.class),
				tree.field("path", String.class)
			)
			.from(tree)
			.orderBy(tree.field("path", String.class))
			.fetch(mapping(CategoryNode::new));
	}
}
