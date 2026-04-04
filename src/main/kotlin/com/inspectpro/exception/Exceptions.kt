package com.inspectpro.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)
class BusinessException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class DuplicateResourceException(message: String) : RuntimeException(message)