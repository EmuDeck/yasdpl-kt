package yasdpl

import react.*

fun <P : Props> JSX(props: P, block: ChildrenBuilder.(P) -> Unit): ReactElement<P>
{
    return createElement(FC(block), props)
}

fun JSX(block: ChildrenBuilder.() -> Unit): ReactElement<Props>
{
    return createElement(VFC(block))
}