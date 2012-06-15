package org.koshinuke.yuzen.gradle

/**
 * @author taichi
 */
class Page {

	def List<Content> contents = []

	def pagingPrefix

	def index

	def pageSize

	Page(List<Content> contents, int index, String pagingPrefix, int pageSize) {
		this.contents = contents
		this.pagingPrefix = pagingPrefix
		this.index = index
		this.pageSize = pageSize
	}

	def getPagination() {
		// page size < 10
		// [1] 2 3 4 5 6 7 8 9 NEXT
		// PREV 1 [2] 3 4 5 6 7 8 9 NEXT
		// PREV 1 2 3 4 5 6 7 8 [9]

		// page size >= 10
		// [1] 2 3 4 5 ... 10 NEXT
		// PREV 1 [2] 3 4 5 ... 10 NEXT
		// PREV 1 2 3 [4] 5 ... 10 NEXT
		// PREV 1 ... 3 4 [5] 6 7 ... 10 NEXT
		// PREV 1 ... 4 5 [6] 7 8 ... 10 NEXT
		// PREV 1 ... 6 [7] 8 9 10 NEXT
		// PREV 1 ... 6 7 8 [9] 10 NEXT
		// PREV 1 ... 6 7 8 9 [10]

		if(this.pageSize < 10) {
			return "small"
		}
		if(this.index < 5) {
			return "dots_right"
		}
		if((this.pageSize - 4) < this.index) {
			return "dots_left"
		}
		return "dots_twice"
	}

	def getPages() {
		if(this.pageSize < 10) {
			return 2..this.pageSize
		}
		def i = this.index + 1
		return (i - 2) .. (i + 2)
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
		def n = this.current + 1
		return "/$pagingPrefix/$n"
	}

}
