# Automatically generated by pb2py
# fmt: off
from .. import protobuf as p

from .MoneroAccountPublicAddress import MoneroAccountPublicAddress

if __debug__:
    try:
        from typing import Dict, List, Optional
        from typing_extensions import Literal  # noqa: F401
    except ImportError:
        Dict, List, Optional = None, None, None  # type: ignore


class MoneroTransactionDestinationEntry(p.MessageType):

    def __init__(
        self,
        amount: int = None,
        addr: MoneroAccountPublicAddress = None,
        is_subaddress: bool = None,
        original: bytes = None,
        is_integrated: bool = None,
    ) -> None:
        self.amount = amount
        self.addr = addr
        self.is_subaddress = is_subaddress
        self.original = original
        self.is_integrated = is_integrated

    @classmethod
    def get_fields(cls) -> Dict:
        return {
            1: ('amount', p.UVarintType, 0),
            2: ('addr', MoneroAccountPublicAddress, 0),
            3: ('is_subaddress', p.BoolType, 0),
            4: ('original', p.BytesType, 0),
            5: ('is_integrated', p.BoolType, 0),
        }
