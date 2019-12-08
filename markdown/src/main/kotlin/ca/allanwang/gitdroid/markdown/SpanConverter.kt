package ca.allanwang.gitdroid.markdown

import android.text.Html
import android.text.Spannable
import android.text.SpannedString
import androidx.core.text.buildSpannedString
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html.RendererBuilder
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.NodeAdaptedVisitor
import com.vladsch.flexmark.util.ast.NodeVisitor

object SpanConverter {



    fun convert(d: Document): SpannedString {
        NodeVisitor(emptyArray()).visit(d)
        NodeAdaptedVisitor
        buildSpannedString {

        }
        Parser.builder()
        Document
    }
}