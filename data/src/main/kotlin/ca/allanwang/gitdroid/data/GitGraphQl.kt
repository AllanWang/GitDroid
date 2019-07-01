package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.gql.GitGraphQlIssuePrs
import ca.allanwang.gitdroid.data.gql.GitGraphQlSearch
import ca.allanwang.gitdroid.data.gql.GitGraphQlUserSearch
import ca.allanwang.gitdroid.data.gql.GitGraphqlProfileScreen

interface GitGraphQl : GitGraphqlProfileScreen, GitGraphQlSearch, GitGraphQlUserSearch, GitGraphQlIssuePrs