package org.koshinuke.yuzen.gradle

/**
 * @author taichi
 */
class Page {

	def List<Content> contents = []

	def pagingPrefix

	def index

	def pageSize

	private paginationMeta

	Page(List<Content> contents, int index, String pagingPrefix, int pageSize) {
		this.contents = contents
		this.pagingPrefix = pagingPrefix
		this.index = index
		this.pageSize = pageSize
		this.paginationMeta = makePaginationMeta()
	}

	def makePaginationMeta() {
		// page size < 10
		// [1] 2 3 4 5 6 7 8 9 NEXT
		// PREV 1 [2] 3 4 5 6 7 8 9 NEXT
		// PREV 1 2 3 4 5 6 7 8 [9]
		if(this.pageSize < 10) {
			return [name: "small", pages: { 2..this.pageSize }]
		}
		// page size >= 10
		// [1] 2 3 4 5 ... 10 NEXT
		// PREV 1 [2] 3 4 5 ... 10 NEXT
		// PREV 1 2 3 [4] 5 ... 10 NEXT
		if(this.index < 4) {
			return [name: "dots_right", pages: { 2..5 }]
		}
		// PREV 1 ... 6 [7] 8 9 10 NEXT
		// PREV 1 ... 6 7 8 [9] 10 NEXT
		// PREV 1 ... 6 7 8 9 [10]
		if((this.pageSize - 5) < this.index) {
			return [name: "dots_left", pages: { (this.pageSize - 4) .. this.pageSize }]
		}
		// PREV 1 ... 3 4 [5] 6 7 ... 10 NEXT
		// PREV 1 ... 4 5 [6] 7 8 ... 10 NEXT
		return [name: "dots_twice", pages: {
				def i = this.index + 1
				return (i - 2) .. (i + 2)
			}]
	}

	def getPagination() {
		return paginationMeta.name
	}

	def getPages() {
		return paginationMeta.pages()
	}

	def getCurrent() {
		this.index + 1
	}

	def isFirst() {
		this.index == 0
	}

	def isLast() {
		this.index == (this.pageSize - 1)
	}

	def getPageUrl(n) {
		"/$pagingPrefix/$n"
	}

	def getPrevUrl() {
		if(isFirst()) {
			return ""
		}
		if(this.index == 1) {
			return "/"
		}
		return "/$pagingPrefix/$index"
	}

	def getNextUrl() {
		if(isLast()) {
			return ""
		}
		def nxt = this.current + 1
		return "/$pagingPrefix/$nxt"
	}

}
