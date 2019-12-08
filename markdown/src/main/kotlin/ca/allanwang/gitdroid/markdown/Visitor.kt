package ca.allanwang.gitdroid.markdown

import android.text.SpannableStringBuilder
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.NodeVisitor
import com.vladsch.flexmark.util.ast.VisitHandler

class Visitor(vararg handlers: VisitHandler<*>) : NodeVisitor(*handlers) {
    val builder = SpannableStringBuilder()

    override fun visit(node: Node?) {
        super.visit(node)
    }
}